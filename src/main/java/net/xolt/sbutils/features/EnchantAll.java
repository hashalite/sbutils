package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.util.Crypt;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.xolt.sbutils.SbUtils.MC;

public class EnchantAll {

    private static final String ENCHANT_COMMAND = "enchantall";
    private static final String ENCHANT_ALIAS = "eall";
    private static final String UNENCHANT_COMMAND = "unenchantall";
    private static final String UNENCHANT_ALIAS = "ueall";

    private static boolean enchanting;
    private static boolean unenchanting;
    private static boolean inventory;
    private static boolean awaitingResponse;
    private static boolean noPermission;
    private static boolean pause;
    private static boolean cooldown;
    private static int prevSelectedSlot;
    private static int itemPrevSlot;
    private static int commandCount;
    private static long lastActionPerformedAt;

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(ENCHANT_COMMAND, ENCHANT_ALIAS, UNENCHANT_COMMAND, UNENCHANT_ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> enchantAllNode = dispatcher.register(
                CommandHelper.runnable(ENCHANT_COMMAND, () -> onEnchantAllCommand(false, false))
                    .then(CommandHelper.runnable("inv", () -> onEnchantAllCommand(false, true)))
                    .then(CommandHelper.genericEnum("mode", "mode", "enchantAll.mode", ModConfig.EnchantMode.class, () -> ModConfig.HANDLER.instance().enchantAll.mode, (value) -> ModConfig.HANDLER.instance().enchantAll.mode = value))
                    .then(CommandHelper.doubl("delay", "seconds", "enchantAll.delay", () -> ModConfig.HANDLER.instance().enchantAll.delay, (value) -> ModConfig.HANDLER.instance().enchantAll.delay = value))
                    .then(CommandHelper.integer("cooldownFrequency", "frequency", "enchantAll.cooldownFrequency", () -> ModConfig.HANDLER.instance().enchantAll.cooldownFrequency, (value) -> ModConfig.HANDLER.instance().enchantAll.cooldownFrequency = value))
                    .then(CommandHelper.doubl("cooldownTime", "seconds", "enchantAll.cooldownTime", () -> ModConfig.HANDLER.instance().enchantAll.cooldownTime, (value) -> ModConfig.HANDLER.instance().enchantAll.cooldownTime = value))
                    .then(CommandHelper.bool("excludeFrost", "enchantAll.excludeFrost", () -> ModConfig.HANDLER.instance().enchantAll.excludeFrost, (value) -> ModConfig.HANDLER.instance().enchantAll.excludeFrost = value))
        );

        final LiteralCommandNode<FabricClientCommandSource> unenchantAllNode = dispatcher.register(
                CommandHelper.runnable(UNENCHANT_COMMAND, () -> onEnchantAllCommand(true, false))
                        .then(CommandHelper.runnable("inv", () -> onEnchantAllCommand(true, true)))
                        .then(CommandHelper.doubl("delay", "seconds", "enchantAll.delay", () -> ModConfig.HANDLER.instance().enchantAll.delay, (value) -> ModConfig.HANDLER.instance().enchantAll.delay = value))
                        .then(CommandHelper.integer("cooldownFrequency", "frequency", "enchantAll.cooldownFrequency", () -> ModConfig.HANDLER.instance().enchantAll.cooldownFrequency, (value) -> ModConfig.HANDLER.instance().enchantAll.cooldownFrequency = value))
                        .then(CommandHelper.doubl("cooldownTime", "seconds", "enchantAll.cooldownTime", () -> ModConfig.HANDLER.instance().enchantAll.cooldownTime, (value) -> ModConfig.HANDLER.instance().enchantAll.cooldownTime = value))
        );

        dispatcher.register(ClientCommandManager.literal(ENCHANT_ALIAS)
                .executes(context ->
                        dispatcher.execute(ENCHANT_COMMAND, context.getSource())
                )
                .redirect(enchantAllNode));

        dispatcher.register(ClientCommandManager.literal(UNENCHANT_ALIAS)
                .executes(context ->
                        dispatcher.execute(UNENCHANT_COMMAND, context.getSource())
                )
                .redirect(unenchantAllNode));
    }

    private static void onEnchantAllCommand(boolean unenchant, boolean inv) {
        if (MC.player == null) {
            return;
        }

        if (enchanting || unenchanting) {
            Messenger.printMessage("message.sbutils.enchantAll.pleaseWait", ChatFormatting.RED);
            return;
        }

        enchanting = !unenchant;
        unenchanting = unenchant;
        prevSelectedSlot = MC.player.getInventory().selected;
        inventory = inv;

        if (done()) {
            if (inv) {
                Messenger.printMessage("message.sbutils.enchantAll.noEnchantableItems", ChatFormatting.RED);
            } else {
                Messenger.printMessage("message.sbutils.enchantAll.itemNotEnchantable", ChatFormatting.RED);
            }
            reset();
            return;
        }

        if (inv && !(MC.player.getMainHandItem().getItem() instanceof AirItem) && getEnchantsForItem(MC.player.getMainHandItem(), unenchant).size() < 1) {
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot < 8) {
                MC.player.getInventory().selected = prevSelectedSlot = enchantableSlot;
                return;
            }
            int emptySlot = InvUtils.findEmptyHotbarSlot();
            if (emptySlot != -1)
                MC.player.getInventory().selected = prevSelectedSlot = emptySlot;
        }
    }

    public static void tick() {
        if ((!enchanting && !unenchanting) || awaitingResponse || MC.player == null) {
            return;
        }

        if (noPermission) {
            if (ModConfig.HANDLER.instance().enchantAll.mode == ModConfig.EnchantMode.ALL && enchanting) {
                Messenger.printMessage("message.sbutils.enchantAll.noEnchantAllPermission", ChatFormatting.RED);
            } else {
                Messenger.printMessage("message.sbutils.enchantAll.noEnchantPermission", ChatFormatting.RED);
            }
            reset();
            return;
        }

        if (MC.player.getInventory().selected != prevSelectedSlot) {
            Messenger.printMessage("message.sbutils.enchantAll.cancelSlotSwitch", ChatFormatting.RED);
            reset();
            return;
        }

        if (done()) {
            if (inventory && itemPrevSlot != -1) {
                AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
                if (!InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler))
                    return;
                InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selected, currentScreenHandler);
            }
            Messenger.printMessage("message.sbutils.enchantAll.complete");
            reset();
            return;
        }

        if (commandCount >= ModConfig.HANDLER.instance().enchantAll.cooldownFrequency) {
            cooldown = true;
            Messenger.printEnchantCooldown(ModConfig.HANDLER.instance().enchantAll.cooldownTime);
            commandCount = 0;
        }

        if (delayLeft() > 0) {
            return;
        }

        if (pause || cooldown) {
            pause = false;
            cooldown = false;
            return;
        }

        if (inventory) {
            doEnchantInv(unenchanting);
        } else {
            doEnchant(unenchanting);
        }
    }

    public static void onDisconnect() {
        reset();
    }

    public static void processMessage(Component message) {
        if ((!enchanting && !unenchanting) || !awaitingResponse) {
            return;
        }

        String messageString = message.getString();
        if (RegexFilters.enchantSingleSuccess.matcher(messageString).matches() ||
                RegexFilters.enchantAllSuccess.matcher(messageString).matches() ||
                RegexFilters.unenchantSuccess.matcher(messageString).matches() ||
                RegexFilters.enchantError.matcher(messageString).matches()) {
            awaitingResponse = false;
        } else if (RegexFilters.enchantNoPermission.matcher(messageString).matches()) {
            noPermission = true;
            awaitingResponse = false;
        }
    }

    private static void doEnchant(boolean unenchant) {
        if (MC.player == null) {
            return;
        }

        ItemStack hand = MC.player.getMainHandItem();

        if (!hand.getItem().isEnchantable(hand)) {
            return;
        }

        List<Enchantment> enchants = getEnchantsForItem(MC.player.getMainHandItem(), unenchant);

        if (enchants.size() < 1) {
            if (shouldRemoveFrost()) {
                sendEnchantCommand(Enchantments.FROST_WALKER, true);
            }
            return;
        }

        sendNextEnchant(enchants, unenchant);
    }

    private static void doEnchantInv(boolean unenchant) {
        if (MC.player == null) {
            return;
        }

        ItemStack hand = MC.player.getMainHandItem();
        List<Enchantment> enchants = getEnchantsForItem(hand, unenchant);

        if (enchants.size() < 1) {
            if (shouldRemoveFrost()) {
                sendEnchantCommand(Enchantments.FROST_WALKER, true);
                return;
            }
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot != -1) {
                AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
                if (itemPrevSlot != -1) {
                    if (!InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler))
                        return;
                    InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selected, currentScreenHandler);
                    itemPrevSlot = -1;
                }
                if (!InvUtils.canSwapSlot(enchantableSlot, currentScreenHandler)) {
                    return;
                }
                InvUtils.swapToHotbar(enchantableSlot, MC.player.getInventory().selected, currentScreenHandler);
                itemPrevSlot = enchantableSlot;
                pause = true;
            }
            return;
        }

        sendNextEnchant(enchants, unenchant);
    }

    private static void sendNextEnchant(List<Enchantment> enchants, boolean unenchant) {
        if (!unenchant && ModConfig.HANDLER.instance().enchantAll.mode == ModConfig.EnchantMode.ALL) {
            sendEnchantAllCommand();
            return;
        }

        sendEnchantCommand(enchants.iterator().next(), unenchant);
    }

    private static void sendEnchantCommand(Enchantment enchantment, boolean unenchant) {
        if (MC.getConnection() == null) {
            return;
        }

        String enchantName = BuiltInRegistries.ENCHANTMENT.getKey(enchantment).getPath().replaceAll("_", "");
        MC.getConnection().sendCommand("enchant " + enchantName + " " + (unenchant ? 0 : enchantment.getMaxLevel()));
        afterSendCommand();
    }

    private static void sendEnchantAllCommand() {
        if (MC.getConnection() == null) {
            return;
        }

        MC.getConnection().send(new ServerboundChatCommandPacket("enchantall", Instant.now(), Crypt.SaltSupplier.getLong(),
                ArgumentSignatures.EMPTY, new LastSeenMessagesTracker(20).generateAndApplyUpdate().update()));
        afterSendCommand();
    }

    private static void afterSendCommand() {
        commandCount++;
        lastActionPerformedAt = System.currentTimeMillis();
        awaitingResponse = true;
    }

    private static int findEnchantableSlot(boolean unenchant) {
        if (MC.player == null) {
            return -1;
        }

        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            if (i >= 36 && i <= 39) {
                // Skip armor slots
                continue;
            }
            ItemStack itemStack = MC.player.getInventory().getItem(i);
            if (getEnchantsForItem(itemStack, unenchant).size() > 0) {
                return i;
            }
        }

        return -1;
    }

    private static List<Enchantment> getEnchantsForItem(ItemStack itemStack, boolean unenchant) {
        Item item = itemStack.getItem();

        if (!itemStack.getItem().isEnchantable(itemStack)) {
            return new ArrayList<>();
        }

        Map<Enchantment, Integer> itemsEnchants = EnchantmentHelper.deserializeEnchantments(itemStack.getEnchantmentTags());
        List<Enchantment> enchantments = new ArrayList<>();
        if (!unenchant) {
            for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
                if (enchantment.canEnchant(new ItemStack(item))) {
                    enchantments.add(enchantment);
                }
            }

            for (Enchantment enchantment : itemsEnchants.keySet()) {
                if (enchantment.getMaxLevel() == itemsEnchants.get(enchantment)) {
                    enchantments.remove(enchantment);
                }
            }

        } else {
            enchantments.addAll(itemsEnchants.keySet());
        }

        enchantments.remove(Enchantments.SILK_TOUCH);
        enchantments.remove(Enchantments.BINDING_CURSE);
        enchantments.remove(Enchantments.VANISHING_CURSE);

        if (ModConfig.HANDLER.instance().enchantAll.excludeFrost && !unenchant) {
            enchantments.remove(Enchantments.FROST_WALKER);
        }

        return enchantments;
    }

    private static int delayLeft() {
        long delay = (long)(ModConfig.HANDLER.instance().enchantAll.delay * 1000.0);
        if (cooldown) {
            delay = (long)(ModConfig.HANDLER.instance().enchantAll.cooldownTime * 1000.0);
        } else if (pause) {
            delay = 250L;
        }

        return (int)Math.max(delay - (System.currentTimeMillis() - lastActionPerformedAt), 0L);
    }

    private static boolean done() {
        if (MC.player == null) {
            return true;
        }

        ItemStack hand = MC.player.getMainHandItem();

        return ((inventory && findEnchantableSlot(unenchanting) == -1) ||
                (!inventory && getEnchantsForItem(hand, unenchanting).size() < 1)) &&
                !shouldRemoveFrost();
    }

    private static boolean shouldRemoveFrost() {
        if (MC.player == null) {
            return false;
        }

        return ModConfig.HANDLER.instance().enchantAll.excludeFrost &&
                EnchantmentHelper.deserializeEnchantments(MC.player.getMainHandItem().getEnchantmentTags()).containsKey(Enchantments.FROST_WALKER);
    }

    private static void reset() {
        enchanting = false;
        unenchanting = false;
        inventory = false;
        awaitingResponse = false;
        noPermission = false;
        pause = false;
        cooldown = false;
        prevSelectedSlot = -1;
        itemPrevSlot = -1;
        commandCount = 0;
    }

    public static boolean active() {
        return enchanting || unenchanting;
    }
}
