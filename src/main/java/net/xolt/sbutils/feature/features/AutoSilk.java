package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.ChatUtils;

import java.util.List;
import java.util.Optional;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoSilk extends Feature {
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 20;

    private final OptionBinding<Boolean> enabled = new OptionBinding<>("autoSilk.enabled", Boolean.class, (config) -> config.autoSilk.enabled, (config, value) -> config.autoSilk.enabled = value);
    private final OptionBinding<ModConfig.SilkTarget> targetTool = new OptionBinding<>("autoSilk.targetTool", ModConfig.SilkTarget.class, (config) -> config.autoSilk.targetTool, (config, value) -> config.autoSilk.targetTool = value);
    private final OptionBinding<Boolean> bookPriority = new OptionBinding<>("autoSilk.bookPriority", Boolean.class, (config) -> config.autoSilk.bookPriority, (config, value) -> config.autoSilk.bookPriority = value);
    private final OptionBinding<Boolean> booksOnly = new OptionBinding<>("autoSilk.booksOnly", Boolean.class, (config) -> config.autoSilk.booksOnly, (config, value) -> config.autoSilk.booksOnly = value);
    private final OptionBinding<Boolean> cleaner = new OptionBinding<>("autoSilk.cleaner", Boolean.class, (config) -> config.autoSilk.cleaner, (config, value) -> config.autoSilk.cleaner = value);
    private final OptionBinding<Double> delay = new OptionBinding<>("autoSilk.delay", Double.class, (config) -> config.autoSilk.delay, (config, value) -> config.autoSilk.delay = value);
    private final OptionBinding<Boolean> showButton = new OptionBinding<>("autoSilk.showButton", Boolean.class, (config) -> config.autoSilk.showButton, (config, value) -> config.autoSilk.showButton = value);
    private final OptionBinding<ModConfig.CornerButtonPos> buttonPos = new OptionBinding<>("autoSilk.buttonPos", ModConfig.CornerButtonPos.class, (config) -> config.autoSilk.buttonPos, (config, value) -> config.autoSilk.buttonPos = value);

    private EnchantState state;
    private long lastActionPerformedAt;
    private EnchantmentMenu screenHandler;
    private boolean cleaning;
    private boolean toolChecked;
    public CycleButton<Boolean> autoSilkButton;

    public AutoSilk() {
        super("autoSilk", "autosilk", "silk");
        reset();
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, targetTool, bookPriority, booksOnly, cleaner, delay, showButton, buttonPos);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoSilkNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                    .then(CommandHelper.genericEnum("target", "tool", targetTool))
                    .then(CommandHelper.bool("bookPriority", bookPriority))
                    .then(CommandHelper.bool("booksOnly", booksOnly))
                    .then(CommandHelper.doubl("delay", "seconds", delay))
                    .then(CommandHelper.bool("showButton", showButton))
                    .then(CommandHelper.genericEnum("buttonPos", "position", buttonPos))
                    .then(CommandHelper.bool("cleaner", cleaner))
        );
        registerAlias(dispatcher, autoSilkNode);
    }

    public void onDisconnect() {
        reset();
    }

    public void onPlayerCloseScreen() {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled || !(MC.screen instanceof EnchantmentScreen))
            return;

        reset();
    }

    public void onEnchantUpdate() {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled)
            return;

        if (state.equals(EnchantState.WAIT_FOR_TOOL_ENCHANTS)) {
            state = EnchantState.ENCHANT_TOOL;
            return;
        }

        if (state.equals(EnchantState.WAIT_FOR_BOOK_ENCHANTS))
            state = EnchantState.ENCHANT_BOOK;
    }

    public void onUpdateInvSlot(ClientboundContainerSetSlotPacket packet) {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled)
            return;

        if (state.equals(EnchantState.WAIT_FOR_ENCHANTING) && packet.getSlot() == 0 && !EnchantmentHelper.getEnchantments(packet.getItem()).isEmpty())
            state = EnchantState.RETURN_ITEM_AND_RESET;
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled || MC.player == null || cleaning)
            return;

        if (System.currentTimeMillis() - lastActionPerformedAt < ModConfig.HANDLER.instance().autoSilk.delay * 1000.0)
            return;

        if (!(MC.screen instanceof EnchantmentScreen)) {
            reset();
            return;
        }

        screenHandler = ((EnchantmentScreen) MC.screen).getMenu();

        if (state == EnchantState.INSERT_LAPIS) {
            if (countFreeSlots(screenHandler) < 1) {
                if (ModConfig.HANDLER.instance().autoSilk.cleaner) {
                    // cleaning must be set before clean() is called, in case callback is called immediately
                    cleaning = true;
                    SbUtils.FEATURES.get(InvCleaner.class).cleanPredicate(AutoSilk::shouldCleanStack, this::onCleanCallback);
                    return;
                }
                ChatUtils.printMessage("message.sbutils.autoSilk.invFull");
                disable();
                return;
            }
            if (getTotalLapis(screenHandler) < 3) {
                if (getTotalLapis(screenHandler) == 0)
                    ChatUtils.printMessage("message.sbutils.autoSilk.noLapis");
                else
                    ChatUtils.printMessage("message.sbutils.autoSilk.notEnoughLapis");
                disable();
                return;
            }
            Item targetTool = ModConfig.HANDLER.instance().autoSilk.targetTool.getTool();
            if (!ModConfig.HANDLER.instance().autoSilk.booksOnly && findInEnchantScreen(targetTool, true, screenHandler) == null) {
                ChatUtils.printWithPlaceholders("message.sbutils.autoSilk.noTools", Component.translatable(targetTool.getDescriptionId()));
                disable();
                return;
            }
            if (findInEnchantScreen(Items.BOOK, true, screenHandler) == null) {
                ChatUtils.printMessage("message.sbutils.autoSilk.noBooks");
                disable();
                return;
            }
        }

        performNextAction();
        lastActionPerformedAt = System.currentTimeMillis();
    }

    private void onCleanCallback(boolean result) {
        cleaning = false;
        if (!result) {
            InvCleaner.showCleanFailedCritical("text.sbutils.config.category.autoSilk");
            disable();
            return;
        }
        if (!clickNearestEnchantTable()) {
            ChatUtils.printMessage("message.sbutils.autoSilk.notCloseEnough");
            disable();
            return;
        }
        lastActionPerformedAt = System.currentTimeMillis();
    }

    private void performNextAction() {
        switch (state) {
            case INSERT_LAPIS:
                insertLapis();
                break;
            case INSERT_TOOL:
                insertTool();
                break;
            case ENCHANT_TOOL:
                enchantPickaxe();
                break;
            case RETURN_ITEM_AND_CONTINUE:
                returnItem(ModConfig.HANDLER.instance().autoSilk.bookPriority && !toolChecked ? EnchantState.INSERT_TOOL : EnchantState.INSERT_BOOK);
                break;
            case INSERT_BOOK:
                insertBook();
                break;
            case ENCHANT_BOOK:
                enchantBook();
                break;
            case RETURN_ITEM_AND_RESET:
                returnItem(EnchantState.INSERT_LAPIS);
                break;
        }
    }

    private void returnItem(EnchantState nextState) {
        if (screenHandler == null)
            return;

        LocalPlayer player = MC.player;
        MultiPlayerGameMode interactionManager = MC.gameMode;
        if (player == null || interactionManager == null)
            return;

        state = nextState;

        if (screenHandler.getSlot(0).getItem().isEmpty())
            // Item already taken
            return;

        interactionManager.handleInventoryMouseClick(screenHandler.containerId, 0, 0, ClickType.QUICK_MOVE, MC.player);
    }

    private void insertLapis() {
        insertItem(Items.LAPIS_LAZULI);
    }

    private void insertTool() {
        insertItem(ModConfig.HANDLER.instance().autoSilk.targetTool.getTool());
    }

    private void enchantPickaxe() {
        enchantItem(false);
    }

    private void insertBook() {
        insertItem(Items.BOOK);
    }

    private void insertItem(Item item) {
        if (screenHandler == null)
            return;

        EnchantState startingItem = ModConfig.HANDLER.instance().autoSilk.bookPriority || ModConfig.HANDLER.instance().autoSilk.booksOnly ? EnchantState.INSERT_BOOK : EnchantState.INSERT_TOOL;

        if (item.equals(Items.LAPIS_LAZULI) && screenHandler.getGoldCount() >= 3) {
            state = startingItem;
            return;
        }

        LocalPlayer player = MC.player;
        MultiPlayerGameMode interactionManager = MC.gameMode;
        if (player == null || interactionManager == null)
            return;

        if (!item.equals(Items.LAPIS_LAZULI) && !screenHandler.getSlot(0).getItem().isEmpty()) {
            // Slot is not empty; remove existing item
            interactionManager.handleInventoryMouseClick(screenHandler.containerId, 0, 0, ClickType.QUICK_MOVE, MC.player);
            return;
        }

        Slot itemSlot = findInEnchantScreen(item, true, screenHandler);
        if (itemSlot == null) {
            if (item.equals(Items.LAPIS_LAZULI))
                ChatUtils.printMessage(getTotalLapis(screenHandler) > 0 ? "message.sbutils.autoSilk.notEnoughLapis" : "message.sbutils.autoSilk.noLapis");
            else if (item.equals(Items.BOOK))
                ChatUtils.printMessage("message.sbutils.autoSilk.noBooks");
            else
                ChatUtils.printWithPlaceholders("message.sbutils.autoSilk.noTools", item.getDescriptionId());
            reset();
            return;
        }

        interactionManager.handleInventoryMouseClick(screenHandler.containerId, itemSlot.index, 0, ClickType.QUICK_MOVE, MC.player);

        if (item.equals(Items.LAPIS_LAZULI))
            if (screenHandler.getGoldCount() >= 3)
                state = startingItem;
            else
                state = EnchantState.INSERT_LAPIS;
        else if (item.equals(Items.BOOK))
            state = EnchantState.WAIT_FOR_BOOK_ENCHANTS;
        else
            state = EnchantState.WAIT_FOR_TOOL_ENCHANTS;
    }

    private void enchantBook() {
        enchantItem(true);
    }

    private void enchantItem(boolean book) {
        if (screenHandler == null)
            return;

        LocalPlayer player = MC.player;
        MultiPlayerGameMode interactionManager = MC.gameMode;
        if (player == null || interactionManager == null)
            return;

        int[] enchantments = screenHandler.enchantClue;
        int silkIndex = -1;
        for (int i = 0; i < enchantments.length; i++) {
            Optional<Holder.Reference<Enchantment>> optionalEnchantment = BuiltInRegistries.ENCHANTMENT.getHolder(enchantments[i]);
            if (optionalEnchantment.isEmpty())
                continue;

            Enchantment enchantment = optionalEnchantment.get().value();

            if (enchantment.equals(Enchantments.SILK_TOUCH)) {
                silkIndex = i;
                break;
            }
        }

        if (!ModConfig.HANDLER.instance().autoSilk.booksOnly && silkIndex == -1 && (!book || (ModConfig.HANDLER.instance().autoSilk.bookPriority && !toolChecked))) {
            // Either tool has no Silk Touch, so we should return and continue to book
            // Or bookPriority is enabled and book has no Silk Touch, so we should return and continue to tool
            // Unless tool has already been checked
            if (!book)
                toolChecked = true;
            state = EnchantState.RETURN_ITEM_AND_CONTINUE;
            return;
        }

        toolChecked = false;

        // If we have gotten to this point and Silk Touch is not present, then the item is a book and should be enchanted with the lowest level enchant
        if (silkIndex == -1)
            silkIndex = 0;

        if (MC.player.experienceLevel < screenHandler.costs[silkIndex]) {
            ChatUtils.printMessage("message.sbutils.autoSilk.notEnoughExperience");
            reset();
            ModConfig.HANDLER.instance().autoSilk.enabled = false;
            ModConfig.HANDLER.save();
            return;
        }

        interactionManager.handleInventoryButtonClick(screenHandler.containerId, silkIndex);
        state = EnchantState.WAIT_FOR_ENCHANTING;
    }

    private void disable() {
        ModConfig.HANDLER.instance().autoSilk.enabled = false;
        ModConfig.HANDLER.save();
        if (autoSilkButton != null)
            autoSilkButton.setValue(false);
        reset();
    }

    private void reset() {
        state = EnchantState.INSERT_LAPIS;
        lastActionPerformedAt = 0;
        screenHandler = null;
        cleaning = false;
        toolChecked = false;
    }

    private static boolean shouldCleanStack(ItemStack stack) {
        return stack.getItem() instanceof EnchantedBookItem && !EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack)).containsKey(Enchantments.SILK_TOUCH);
    }

    private static boolean clickNearestEnchantTable() {
        if (MC.player == null || MC.level == null || MC.gameMode == null)
            return false;

        BlockPos playerPos = MC.player.blockPosition();
        BlockPos tablePos = null;
        int range = 4; // FIXME Hardcoded for now
        for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; x++) {
            for (int y = playerPos.getY() - range; y <= playerPos.getY() + range; y++) {
                for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = MC.level.getBlockState(pos);
                    if (state.getBlock() instanceof EnchantmentTableBlock)
                        tablePos = pos;
                }
            }
        }

        if (tablePos == null)
            return false;

        MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, new BlockHitResult(tablePos.getCenter(), Direction.UP, tablePos, false));
        return true;
    }

    private static Slot findInEnchantScreen(Item item, boolean ignoreEnchantingSlots, EnchantmentMenu screenHandler) {
        if (screenHandler == null)
            return null;

        Slot result = null;
        for (Slot slot : screenHandler.slots) {
            if (ignoreEnchantingSlots && slot.index < 2)
                continue;
            ItemStack itemStack = slot.getItem();
            if (itemStack.getItem().equals(item) && itemStack.getEnchantmentTags().isEmpty()) {
                if (item.getMaxStackSize() == 1)
                    return slot;
                if (result == null) {
                    result = slot;
                    continue;
                }
                if (result.getItem().getCount() > slot.getItem().getCount())
                    result = slot;
            }
        }
        return result;
    }

    private static int getTotalLapis(EnchantmentMenu screenHandler) {
        if (screenHandler == null) {
            return -1;
        }

        int total = 0;
        for (Slot slot : screenHandler.slots) {
            ItemStack itemStack = slot.getItem();
            if (itemStack.getItem().equals(Items.LAPIS_LAZULI))
                total += itemStack.getCount();
        }
        return total;
    }

    private static int countFreeSlots(EnchantmentMenu screenHandler) {
        if (screenHandler == null)
            return -1;

        int total = 0;
        for (Slot slot : screenHandler.slots) {
            if (slot.index < 2)
                continue;
            ItemStack itemStack = slot.getItem();
            if (itemStack.isEmpty())
                total++;
        }

        if (!screenHandler.getSlot(0).getItem().isEmpty())
            total--;

        ItemStack lapis = screenHandler.getSlot(1).getItem();
        if (!lapis.isEmpty() && countFreeLapisSlots(screenHandler) < lapis.getCount())
            total--;

        return total;
    }

    private static int countFreeLapisSlots(EnchantmentMenu screenHandler) {
        if (screenHandler == null)
            return -1;

        int total = 0;
        for (Slot slot : screenHandler.slots) {
            if (slot.index < 2)
                continue;
            ItemStack itemStack = slot.getItem();
            if (itemStack.getItem() == Items.LAPIS_LAZULI)
                total += itemStack.getMaxStackSize() - itemStack.getCount();
        }
        return total;
    }

    private enum EnchantState {
        INSERT_LAPIS,
        INSERT_TOOL,
        WAIT_FOR_TOOL_ENCHANTS,
        ENCHANT_TOOL,
        RETURN_ITEM_AND_CONTINUE,
        INSERT_BOOK,
        WAIT_FOR_BOOK_ENCHANTS,
        ENCHANT_BOOK,
        WAIT_FOR_ENCHANTING,
        RETURN_ITEM_AND_RESET;
    }
}
