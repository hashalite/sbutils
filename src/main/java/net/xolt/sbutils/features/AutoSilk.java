package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
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
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;

import java.util.Optional;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoSilk {
    private static State state;
    private static long lastActionPerformedAt;
    private static EnchantmentScreenHandler screenHandler;


    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> autoSilkNode = dispatcher.register(ClientCommandManager.literal("autosilk")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoSilk = !ModConfig.INSTANCE.getConfig().autoSilk;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autosilk", ModConfig.INSTANCE.getConfig().autoSilk);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoSilkDelay", ModConfig.INSTANCE.getConfig().autoSilkDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoSilkDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoSilkDelay", ModConfig.INSTANCE.getConfig().autoSilkDelay);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("silk")
                .executes(context ->
                    dispatcher.execute("autosilk", context.getSource())
                )
                .redirect(autoSilkNode));
    }

    public static void onPlayerCloseScreen() {
        if (!ModConfig.INSTANCE.getConfig().autoSilk || !(MC.currentScreen instanceof EnchantmentScreen)) {
            return;
        }

        reset();
    }

    public static void onEnchantUpdate() {
        if (!ModConfig.INSTANCE.getConfig().autoSilk) {
            return;
        }

        if (state.equals(State.WAIT_FOR_PICK_ENCHANTS)) {
            state = State.ENCHANT_PICKAXE;
            return;
        }

        if (state.equals(State.WAIT_FOR_BOOK_ENCHANTS)) {
            state = State.ENCHANT_BOOK;
        }
    }

    public static void onInventoryUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
        if (!ModConfig.INSTANCE.getConfig().autoSilk) {
            return;
        }

        if (state.equals(State.WAIT_FOR_ENCHANTING) && packet.getSlot() == 0 && EnchantmentHelper.get(packet.getItemStack()).size() > 0) {
            state = State.RETURN_ITEM_AND_RESET;
        }
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().autoSilk || MC.player == null) {
            return;
        }

        if (System.currentTimeMillis() - lastActionPerformedAt < ModConfig.INSTANCE.getConfig().autoSilkDelay * 1000.0) {
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
                ModConfig.INSTANCE.getConfig().autoSilk = false;
                ModConfig.INSTANCE.save();
                reset();
                return;
            }
            if (getTotalLapis() == 0) {
                Messenger.printMessage("message.sbutils.autoSilk.noLapis");
                ModConfig.INSTANCE.getConfig().autoSilk = false;
                ModConfig.INSTANCE.save();
                reset();
                return;
            }
            if (getTotalLapis() < 3) {
                Messenger.printMessage("message.sbutils.autoSilk.notEnoughLapis");
                ModConfig.INSTANCE.getConfig().autoSilk = false;
                ModConfig.INSTANCE.save();
                reset();
                return;
            }
            if (findInEnchantScreen(Items.DIAMOND_PICKAXE, true) == null) {
                Messenger.printMessage("message.sbutils.autoSilk.noPickaxes");
                ModConfig.INSTANCE.getConfig().autoSilk = false;
                ModConfig.INSTANCE.save();
                reset();
                return;
            }
            if (findInEnchantScreen(Items.BOOK, true) == null) {
                Messenger.printMessage("message.sbutils.autoSilk.noBooks");
                ModConfig.INSTANCE.getConfig().autoSilk = false;
                ModConfig.INSTANCE.save();
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
            case INSERT_PICKAXE:
                insertPickaxe();
                break;
            case ENCHANT_PICKAXE:
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

    private static void insertPickaxe() {
        insertItem(Items.DIAMOND_PICKAXE);
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
            state = State.INSERT_PICKAXE;
            return;
        }

        ClientPlayerEntity player = MC.player;
        ClientPlayerInteractionManager interactionManager = MC.interactionManager;
        if (player == null || interactionManager == null) {
            return;
        }

        if ((item.equals(Items.BOOK) || item.equals(Items.DIAMOND_PICKAXE)) && !screenHandler.getSlot(0).getStack().isEmpty()) {
            // Slot is not empty
            interactionManager.clickSlot(screenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, MC.player);
            return;
        }

        Slot itemSlot = findInEnchantScreen(item, true);
        if (itemSlot == null) {
            if (item.equals(Items.BOOK)) {
                Messenger.printMessage("message.sbutils.autoSilk.noBooks");
            } else if (item.equals(Items.DIAMOND_PICKAXE)) {
                Messenger.printMessage("message.sbutils.autoSilk.noPickaxes");
            } else if (item.equals(Items.LAPIS_LAZULI)) {
                Messenger.printMessage(getTotalLapis() > 0 ? "message.sbutils.autoSilk.notEnoughLapis" : "message.sbutils.autoSilk.noLapis");
            }
            reset();
            return;
        }

        interactionManager.clickSlot(screenHandler.syncId, itemSlot.id, 0, SlotActionType.QUICK_MOVE, MC.player);

        if (item.equals(Items.BOOK)) {
            state = State.WAIT_FOR_BOOK_ENCHANTS;
        } else if (item.equals(Items.DIAMOND_PICKAXE)) {
            state = State.WAIT_FOR_PICK_ENCHANTS;
        } else if (item.equals(Items.LAPIS_LAZULI)) {
            if (screenHandler.getLapisCount() >= 3) {
                state = State.INSERT_PICKAXE;
            } else {
                state = State.INSERT_LAPIS;
            }
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
            // No silktouch for pickaxe, continue to book
            state = State.RETURN_ITEM_AND_CONTINUE;
            return;
        }

        if (MC.player.experienceLevel < screenHandler.enchantmentPower[buttonIndex]) {
            Messenger.printMessage("message.sbutils.autoSilk.notEnoughExperience");
            reset();
            ModConfig.INSTANCE.getConfig().autoSilk = false;
            ModConfig.INSTANCE.save();
            return;
        }

        interactionManager.clickButton(screenHandler.syncId, buttonIndex);
        state = State.WAIT_FOR_ENCHANTING;
    }

    private static Slot findInEnchantScreen(Item item, boolean ignoreEnchantingSlots) {
        if (screenHandler == null) {
            return null;
        }

        for (Slot slot : screenHandler.slots) {
            if (ignoreEnchantingSlots && slot.id < 2) {
                continue;
            }
            ItemStack itemStack = slot.getStack();
            if (itemStack.getItem().equals(item) && itemStack.getEnchantments().isEmpty()) {
                return slot;
            }
        }
        return null;
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

        if (!screenHandler.getSlot(1).getStack().isEmpty()) {
            total--;
        }

        return total;
    }

    private enum State {
        INSERT_LAPIS,
        INSERT_PICKAXE,
        WAIT_FOR_PICK_ENCHANTS,
        ENCHANT_PICKAXE,
        RETURN_ITEM_AND_CONTINUE,
        INSERT_BOOK,
        WAIT_FOR_BOOK_ENCHANTS,
        ENCHANT_BOOK,
        WAIT_FOR_ENCHANTING,
        RETURN_ITEM_AND_RESET;
    }

    public static void reset() {
        state = State.INSERT_LAPIS;
        lastActionPerformedAt = 0;
        screenHandler = null;
    }
}
