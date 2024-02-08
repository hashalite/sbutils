package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
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
    private static EnchantmentScreenHandler screenHandler;
    public static CyclingButtonWidget<Boolean> autoSilkButton;

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoSilkNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoSilk", () -> ModConfig.HANDLER.instance().autoSilk.enabled, (value) -> ModConfig.HANDLER.instance().autoSilk.enabled = value)
                    .then(CommandUtils.getterSetter("target", "tool", "autoSilk.targetTool", () -> ModConfig.HANDLER.instance().autoSilk.targetTool, (value) -> ModConfig.HANDLER.instance().autoSilk.targetTool = value, ModConfig.SilkTarget.SilkTargetArgumentType.silkTarget(), ModConfig.SilkTarget.SilkTargetArgumentType::getSilkTarget))
                    .then(CommandUtils.doubl("delay", "seconds", "autoSilk.delay", () -> ModConfig.HANDLER.instance().autoSilk.delay, (value) -> ModConfig.HANDLER.instance().autoSilk.delay = value))
                    .then(CommandUtils.bool("showButton", "autoSilk.showButton", () -> ModConfig.HANDLER.instance().autoSilk.showButton, (value) -> ModConfig.HANDLER.instance().autoSilk.showButton = value))
                    .then(CommandUtils.getterSetter("buttonPos", "position", "autoSilk.buttonPos", () -> ModConfig.HANDLER.instance().autoSilk.buttonPos, (value) -> ModConfig.HANDLER.instance().autoSilk.buttonPos = value, ModConfig.CornerButtonPos.CornerButtonPosArgumentType.cornerButtonPos(), ModConfig.CornerButtonPos.CornerButtonPosArgumentType::getCornerButtonPos))
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
        if (!ModConfig.HANDLER.instance().autoSilk.enabled || !(MC.currentScreen instanceof EnchantmentScreen)) {
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

    public static void onUpdateInvSlot(ScreenHandlerSlotUpdateS2CPacket packet) {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled) {
            return;
        }

        if (state.equals(State.WAIT_FOR_ENCHANTING) && packet.getSlot() == 0 && !EnchantmentHelper.get(packet.getStack()).isEmpty()) {
            state = State.RETURN_ITEM_AND_RESET;
        }
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoSilk.enabled || MC.player == null) {
            return;
        }

        if (System.currentTimeMillis() - lastActionPerformedAt < ModConfig.HANDLER.instance().autoSilk.delay * 1000.0) {
            return;
        }

        if (!(MC.currentScreen instanceof EnchantmentScreen)) {
            reset();
            return;
        }

        screenHandler = ((EnchantmentScreen) MC.currentScreen).getScreenHandler();

        if (state == State.INSERT_LAPIS) {
            if (countFreeSlots() < 1) {
                Messenger.printMessage("message.sbutils.autoSilk.invFull");
                disable();
                reset();
                return;
            }
            if (getTotalLapis() < 3) {
                if (getTotalLapis() == 0) {
                    Messenger.printMessage("message.sbutils.autoSilk.noLapis");
                } else {
                    Messenger.printMessage("message.sbutils.autoSilk.notEnoughLapis");
                }
                disable();
                reset();
                return;
            }
            Item targetTool = ModConfig.HANDLER.instance().autoSilk.targetTool.getTool();
            if (findInEnchantScreen(targetTool, true) == null) {
                Messenger.printWithPlaceholders("message.sbutils.autoSilk.noTools", targetTool.getTranslationKey());
                disable();
                reset();
                return;
            }
            if (findInEnchantScreen(Items.BOOK, true) == null) {
                Messenger.printMessage("message.sbutils.autoSilk.noBooks");
                disable();
                reset();
                return;
            }
        }

        performNextAction();
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

        ClientPlayerEntity player = MC.player;
        ClientPlayerInteractionManager interactionManager = MC.interactionManager;
        if (player == null || interactionManager == null) {
            return;
        }

        state = nextState;

        if (screenHandler.getSlot(0).getStack().isEmpty()) {
            // Item already taken
            return;
        }

        interactionManager.clickSlot(screenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, MC.player);
    }

    private static void insertItem(Item item) {
        if (screenHandler == null) {
            return;
        }

        if (item.equals(Items.LAPIS_LAZULI) && screenHandler.getLapisCount() >= 3) {
            state = State.INSERT_TOOL;
            return;
        }

        ClientPlayerEntity player = MC.player;
        ClientPlayerInteractionManager interactionManager = MC.interactionManager;
        if (player == null || interactionManager == null) {
            return;
        }

        if (!item.equals(Items.LAPIS_LAZULI) && !screenHandler.getSlot(0).getStack().isEmpty()) {
            // Slot is not empty; remove existing item
            interactionManager.clickSlot(screenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, MC.player);
            return;
        }

        Slot itemSlot = findInEnchantScreen(item, true);
        if (itemSlot == null) {
            if (item.equals(Items.LAPIS_LAZULI)) {
                Messenger.printMessage(getTotalLapis() > 0 ? "message.sbutils.autoSilk.notEnoughLapis" : "message.sbutils.autoSilk.noLapis");
            } else if (item.equals(Items.BOOK)) {
                Messenger.printMessage("message.sbutils.autoSilk.noBooks");
            } else {
                Messenger.printWithPlaceholders("message.sbutils.autoSilk.noTools", item.getTranslationKey());
            }
            reset();
            return;
        }

        interactionManager.clickSlot(screenHandler.syncId, itemSlot.id, 0, SlotActionType.QUICK_MOVE, MC.player);

        if (item.equals(Items.LAPIS_LAZULI)) {
            if (screenHandler.getLapisCount() >= 3) {
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

        ClientPlayerEntity player = MC.player;
        ClientPlayerInteractionManager interactionManager = MC.interactionManager;
        if (player == null || interactionManager == null) {
            return;
        }

        int[] enchantments = screenHandler.enchantmentId;
        int buttonIndex = book ? 0 : -1;
        for (int i = 0; i < enchantments.length; i++) {
            Optional<RegistryEntry.Reference<Enchantment>> optionalEnchantment = Registries.ENCHANTMENT.getEntry(enchantments[i]);
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

        if (MC.player.experienceLevel < screenHandler.enchantmentPower[buttonIndex]) {
            Messenger.printMessage("message.sbutils.autoSilk.notEnoughExperience");
            reset();
            ModConfig.HANDLER.instance().autoSilk.enabled = false;
            ModConfig.HANDLER.save();
            return;
        }

        interactionManager.clickButton(screenHandler.syncId, buttonIndex);
        state = State.WAIT_FOR_ENCHANTING;
    }

    private static Slot findInEnchantScreen(Item item, boolean ignoreEnchantingSlots) {
        if (screenHandler == null) {
            return null;
        }

        Slot result = null;
        for (Slot slot : screenHandler.slots) {
            if (ignoreEnchantingSlots && slot.id < 2) {
                continue;
            }
            ItemStack itemStack = slot.getStack();
            if (itemStack.getItem().equals(item) && itemStack.getEnchantments().isEmpty()) {
                if (item.getMaxCount() == 1)
                    return slot;
                if (result == null) {
                    result = slot;
                    continue;
                }
                if (result.getStack().getCount() > slot.getStack().getCount()) {
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
            ItemStack itemStack = slot.getStack();
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
            if (slot.id < 2) {
                continue;
            }
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmpty()) {
                total++;
            }
        }

        if (!screenHandler.getSlot(0).getStack().isEmpty()) {
            total--;
        }

        ItemStack lapis = screenHandler.getSlot(1).getStack();
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
            if (slot.id < 2) {
                continue;
            }
            ItemStack itemStack = slot.getStack();
            if (itemStack.getItem() == Items.LAPIS_LAZULI) {
                total += itemStack.getMaxCount() - itemStack.getCount();
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
    }
}
