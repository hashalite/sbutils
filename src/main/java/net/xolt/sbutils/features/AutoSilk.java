package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.xolt.sbutils.features.common.InvCleaner;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.Messenger;

import java.util.List;
import java.util.Optional;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoSilk {

    private static final String COMMAND = "autosilk";
    private static final String ALIAS = "silk";
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 20;

    private static State state;
    private static long lastActionPerformedAt;
    private static EnchantmentMenu screenHandler;
    private static boolean cleaning;
    public static CycleButton<Boolean> autoSilkButton;

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoSilkNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoSilk", () -> ModConfig.HANDLER.instance().autoSilk.enabled, (value) -> ModConfig.HANDLER.instance().autoSilk.enabled = value)
                    .then(CommandHelper.genericEnum("target", "tool", "autoSilk.targetTool", ModConfig.SilkTarget.class, () -> ModConfig.HANDLER.instance().autoSilk.targetTool, (value) -> ModConfig.HANDLER.instance().autoSilk.targetTool = value))
                    .then(CommandHelper.doubl("delay", "seconds", "autoSilk.delay", () -> ModConfig.HANDLER.instance().autoSilk.delay, (value) -> ModConfig.HANDLER.instance().autoSilk.delay = value))
                    .then(CommandHelper.bool("showButton", "autoSilk.showButton", () -> ModConfig.HANDLER.instance().autoSilk.showButton, (value) -> ModConfig.HANDLER.instance().autoSilk.showButton = value))
                    .then(CommandHelper.genericEnum("buttonPos", "position", "autoSilk.buttonPos", ModConfig.CornerButtonPos.class, () -> ModConfig.HANDLER.instance().autoSilk.buttonPos, (value) -> ModConfig.HANDLER.instance().autoSilk.buttonPos = value))
                    .then(CommandHelper.bool("cleaner", "autoSilk.cleaner", () -> ModConfig.HANDLER.instance().autoSilk.cleaner, (value) -> ModConfig.HANDLER.instance().autoSilk.cleaner = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                    dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoSilkNode));
    }

    public static void onDisconnect() {
        reset();
    }

    public static void onPlayerCloseScreen() {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled || !(MC.screen instanceof EnchantmentScreen)) {
            return;
        }

        reset();
    }

    public static void onEnchantUpdate() {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled) {
            return;
        }

        if (state.equals(State.WAIT_FOR_TOOL_ENCHANTS)) {
            state = State.ENCHANT_TOOL;
            return;
        }

        if (state.equals(State.WAIT_FOR_BOOK_ENCHANTS)) {
            state = State.ENCHANT_BOOK;
        }
    }

    public static void onUpdateInvSlot(ClientboundContainerSetSlotPacket packet) {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled) {
            return;
        }

        if (state.equals(State.WAIT_FOR_ENCHANTING) && packet.getSlot() == 0 && !EnchantmentHelper.getEnchantments(packet.getItem()).isEmpty()) {
            state = State.RETURN_ITEM_AND_RESET;
        }
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled || MC.player == null || cleaning)
            return;

        if (System.currentTimeMillis() - lastActionPerformedAt < ModConfig.HANDLER.instance().autoSilk.delay * 1000.0)
            return;

        if (!(MC.screen instanceof EnchantmentScreen)) {
            reset();
            return;
        }

        screenHandler = ((EnchantmentScreen) MC.screen).getMenu();

        if (state == State.INSERT_LAPIS) {
            if (countFreeSlots() < 1) {
                if (ModConfig.HANDLER.instance().autoSilk.cleaner) {
                    // cleaning must be set before clean() is called, in case callback is called immediately
                    cleaning = true;
                    InvCleaner.clean(AutoSilk::shouldCleanStack, AutoSilk::onCleanCallback);
                    return;
                }
                Messenger.printMessage("message.sbutils.autoSilk.invFull");
                disable();
                return;
            }
            if (getTotalLapis() < 3) {
                if (getTotalLapis() == 0) {
                    Messenger.printMessage("message.sbutils.autoSilk.noLapis");
                } else {
                    Messenger.printMessage("message.sbutils.autoSilk.notEnoughLapis");
                }
                disable();
                return;
            }
            Item targetTool = ModConfig.HANDLER.instance().autoSilk.targetTool.getTool();
            if (findInEnchantScreen(targetTool, true) == null) {
                Messenger.printWithPlaceholders("message.sbutils.autoSilk.noTools", targetTool.getDescriptionId());
                disable();
                return;
            }
            if (findInEnchantScreen(Items.BOOK, true) == null) {
                Messenger.printMessage("message.sbutils.autoSilk.noBooks");
                disable();
                return;
            }
        }

        performNextAction();
        lastActionPerformedAt = System.currentTimeMillis();
    }

    private static boolean shouldCleanStack(ItemStack stack) {
        return stack.getItem() instanceof EnchantedBookItem && !EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack)).containsKey(Enchantments.SILK_TOUCH);
    }

    private static void onCleanCallback(boolean result) {
        cleaning = false;
        if (!result) {
            Messenger.printInvCleanFailed("text.sbutils.config.category.autoSilk");
            disable();
            return;
        }
        clickNearestEnchantTable();
    }

    private static void clickNearestEnchantTable() {
        if (MC.player == null || MC.level == null || MC.gameMode == null) {
            return;
        }

        BlockPos playerPos = MC.player.blockPosition();
        BlockPos tablePos = null;
        int range = 4; // FIXME Hardcoded for now
        for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; x++) {
            for (int y = playerPos.getY() - range; y <= playerPos.getY() + range; y++) {
                for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = MC.level.getBlockState(pos);
                    if (state.getBlock() instanceof EnchantmentTableBlock) {
                        tablePos = pos;
                    }
                }
            }
        }

        if (tablePos == null) {
            Messenger.printMessage("message.sbutils.autoSilk.notCloseEnough");
            disable();
            return;
        }

        MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, new BlockHitResult(tablePos.getCenter(), Direction.UP, tablePos, false));
        lastActionPerformedAt = System.currentTimeMillis();
    }

    private static void performNextAction() {
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
                returnItem(State.INSERT_BOOK);
                break;
            case INSERT_BOOK:
                insertBook();
                break;
            case ENCHANT_BOOK:
                enchantBook();
                break;
            case RETURN_ITEM_AND_RESET:
                returnItem(State.INSERT_LAPIS);
                break;
        }
    }

    private static void insertLapis() {
        insertItem(Items.LAPIS_LAZULI);
    }

    private static void insertTool() {
        insertItem(ModConfig.HANDLER.instance().autoSilk.targetTool.getTool());
    }

    private static void enchantPickaxe() {
        enchantItem(false);
    }

    private static void insertBook() {
        insertItem(Items.BOOK);
    }

    private static void enchantBook() {
        enchantItem(true);
    }

    private static void returnItem(State nextState) {
        if (screenHandler == null) {
            return;
        }

        LocalPlayer player = MC.player;
        MultiPlayerGameMode interactionManager = MC.gameMode;
        if (player == null || interactionManager == null) {
            return;
        }

        state = nextState;

        if (screenHandler.getSlot(0).getItem().isEmpty()) {
            // Item already taken
            return;
        }

        interactionManager.handleInventoryMouseClick(screenHandler.containerId, 0, 0, ClickType.QUICK_MOVE, MC.player);
    }

    private static void insertItem(Item item) {
        if (screenHandler == null) {
            return;
        }

        if (item.equals(Items.LAPIS_LAZULI) && screenHandler.getGoldCount() >= 3) {
            state = State.INSERT_TOOL;
            return;
        }

        LocalPlayer player = MC.player;
        MultiPlayerGameMode interactionManager = MC.gameMode;
        if (player == null || interactionManager == null) {
            return;
        }

        if (!item.equals(Items.LAPIS_LAZULI) && !screenHandler.getSlot(0).getItem().isEmpty()) {
            // Slot is not empty; remove existing item
            interactionManager.handleInventoryMouseClick(screenHandler.containerId, 0, 0, ClickType.QUICK_MOVE, MC.player);
            return;
        }

        Slot itemSlot = findInEnchantScreen(item, true);
        if (itemSlot == null) {
            if (item.equals(Items.LAPIS_LAZULI)) {
                Messenger.printMessage(getTotalLapis() > 0 ? "message.sbutils.autoSilk.notEnoughLapis" : "message.sbutils.autoSilk.noLapis");
            } else if (item.equals(Items.BOOK)) {
                Messenger.printMessage("message.sbutils.autoSilk.noBooks");
            } else {
                Messenger.printWithPlaceholders("message.sbutils.autoSilk.noTools", item.getDescriptionId());
            }
            reset();
            return;
        }

        interactionManager.handleInventoryMouseClick(screenHandler.containerId, itemSlot.index, 0, ClickType.QUICK_MOVE, MC.player);

        if (item.equals(Items.LAPIS_LAZULI)) {
            if (screenHandler.getGoldCount() >= 3) {
                state = State.INSERT_TOOL;
            } else {
                state = State.INSERT_LAPIS;
            }
        } else if (item.equals(Items.BOOK)) {
            state = State.WAIT_FOR_BOOK_ENCHANTS;
        } else {
            state = State.WAIT_FOR_TOOL_ENCHANTS;
        }
    }

    private static void enchantItem(boolean book) {
        if (screenHandler == null) {
            return;
        }

        LocalPlayer player = MC.player;
        MultiPlayerGameMode interactionManager = MC.gameMode;
        if (player == null || interactionManager == null) {
            return;
        }

        int[] enchantments = screenHandler.enchantClue;
        int buttonIndex = book ? 0 : -1;
        for (int i = 0; i < enchantments.length; i++) {
            Optional<Holder.Reference<Enchantment>> optionalEnchantment = BuiltInRegistries.ENCHANTMENT.getHolder(enchantments[i]);
            if (!optionalEnchantment.isPresent()) {
                continue;
            }

            Enchantment enchantment = optionalEnchantment.get().value();

            if (enchantment.equals(Enchantments.SILK_TOUCH)) {
                buttonIndex = i;
            }
        }

        if (buttonIndex == -1) {
            // No silktouch for tool, continue to book
            state = State.RETURN_ITEM_AND_CONTINUE;
            return;
        }

        if (MC.player.experienceLevel < screenHandler.costs[buttonIndex]) {
            Messenger.printMessage("message.sbutils.autoSilk.notEnoughExperience");
            reset();
            ModConfig.HANDLER.instance().autoSilk.enabled = false;
            ModConfig.HANDLER.save();
            return;
        }

        interactionManager.handleInventoryButtonClick(screenHandler.containerId, buttonIndex);
        state = State.WAIT_FOR_ENCHANTING;
    }

    private static Slot findInEnchantScreen(Item item, boolean ignoreEnchantingSlots) {
        if (screenHandler == null) {
            return null;
        }

        Slot result = null;
        for (Slot slot : screenHandler.slots) {
            if (ignoreEnchantingSlots && slot.index < 2) {
                continue;
            }
            ItemStack itemStack = slot.getItem();
            if (itemStack.getItem().equals(item) && itemStack.getEnchantmentTags().isEmpty()) {
                if (item.getMaxStackSize() == 1)
                    return slot;
                if (result == null) {
                    result = slot;
                    continue;
                }
                if (result.getItem().getCount() > slot.getItem().getCount()) {
                    result = slot;
                }
            }
        }
        return result;
    }

    private static int getTotalLapis() {
        if (screenHandler == null) {
            return -1;
        }

        int total = 0;
        for (Slot slot : screenHandler.slots) {
            ItemStack itemStack = slot.getItem();
            if (itemStack.getItem().equals(Items.LAPIS_LAZULI)) {
                total += itemStack.getCount();
            }
        }
        return total;
    }

    private static int countFreeSlots() {
        if (screenHandler == null) {
            return -1;
        }

        int total = 0;
        for (Slot slot : screenHandler.slots) {
            if (slot.index < 2) {
                continue;
            }
            ItemStack itemStack = slot.getItem();
            if (itemStack.isEmpty()) {
                total++;
            }
        }

        if (!screenHandler.getSlot(0).getItem().isEmpty()) {
            total--;
        }

        ItemStack lapis = screenHandler.getSlot(1).getItem();
        if (!lapis.isEmpty() && countFreeLapisSlots() < lapis.getCount()) {
            total--;
        }

        return total;
    }

    private static int countFreeLapisSlots() {
        if (screenHandler == null) {
            return -1;
        }

        int total = 0;
        for (Slot slot : screenHandler.slots) {
            if (slot.index < 2) {
                continue;
            }
            ItemStack itemStack = slot.getItem();
            if (itemStack.getItem() == Items.LAPIS_LAZULI) {
                total += itemStack.getMaxStackSize() - itemStack.getCount();
            }
        }
        return total;
    }

    private static void disable() {
        ModConfig.HANDLER.instance().autoSilk.enabled = false;
        ModConfig.HANDLER.save();
        if (autoSilkButton != null) {
            autoSilkButton.setValue(false);
        }
        reset();
    }

    private enum State {
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

    private static void reset() {
        state = State.INSERT_LAPIS;
        lastActionPerformedAt = 0;
        screenHandler = null;
        cleaning = false;
    }
}
