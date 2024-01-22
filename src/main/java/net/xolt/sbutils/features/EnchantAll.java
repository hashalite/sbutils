package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
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
                CommandUtils.runnable(ENCHANT_COMMAND, () -> onEnchantAllCommand(false, false))
                    .then(CommandUtils.runnable("inv", () -> onEnchantAllCommand(false, true)))
                    .then(CommandUtils.getterSetter("mode", "mode", "enchantMode", () -> ModConfig.HANDLER.instance().enchantMode, (value) -> ModConfig.HANDLER.instance().enchantMode = value, ModConfig.EnchantMode.EnchantModeArgumentType.enchantMode(), ModConfig.EnchantMode.EnchantModeArgumentType::getEnchantMode))
                    .then(CommandUtils.doubl("delay", "seconds", "enchantDelay", () -> ModConfig.HANDLER.instance().enchantDelay, (value) -> ModConfig.HANDLER.instance().enchantDelay = value))
                    .then(CommandUtils.integer("cooldownFrequency", "frequency", "cooldownFrequency", () -> ModConfig.HANDLER.instance().cooldownFrequency, (value) -> ModConfig.HANDLER.instance().cooldownFrequency = value))
                    .then(CommandUtils.doubl("cooldownTime", "seconds", "cooldownTime", () -> ModConfig.HANDLER.instance().cooldownTime, (value) -> ModConfig.HANDLER.instance().cooldownTime = value))
                    .then(CommandUtils.bool("excludeFrost", "excludeFrost", () -> ModConfig.HANDLER.instance().excludeFrost, (value) -> ModConfig.HANDLER.instance().excludeFrost = value))
        );

        final LiteralCommandNode<FabricClientCommandSource> unenchantAllNode = dispatcher.register(
                CommandUtils.runnable(UNENCHANT_COMMAND, () -> onEnchantAllCommand(true, false))
                        .then(CommandUtils.runnable("inv", () -> onEnchantAllCommand(true, true)))
                        .then(CommandUtils.doubl("delay", "seconds", "enchantDelay", () -> ModConfig.HANDLER.instance().enchantDelay, (value) -> ModConfig.HANDLER.instance().enchantDelay = value))
                        .then(CommandUtils.integer("cooldownFrequency", "frequency", "cooldownFrequency", () -> ModConfig.HANDLER.instance().cooldownFrequency, (value) -> ModConfig.HANDLER.instance().cooldownFrequency = value))
                        .then(CommandUtils.doubl("cooldownTime", "seconds", "cooldownTime", () -> ModConfig.HANDLER.instance().cooldownTime, (value) -> ModConfig.HANDLER.instance().cooldownTime = value))
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
            Messenger.printMessage("message.sbutils.enchantAll.pleaseWait", Formatting.RED);
            return;
        }

        enchanting = !unenchant;
        unenchanting = unenchant;
        prevSelectedSlot = MC.player.getInventory().selectedSlot;
        inventory = inv;

        if (done()) {
            if (inv) {
                Messenger.printMessage("message.sbutils.enchantAll.noEnchantableItems", Formatting.RED);
            } else {
                Messenger.printMessage("message.sbutils.enchantAll.itemNotEnchantable", Formatting.RED);
            }
            reset();
            return;
        }

        if (inv && !(MC.player.getMainHandStack().getItem() instanceof AirBlockItem) && getEnchantsForItem(MC.player.getMainHandStack(), unenchant).size() < 1) {
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot < 8) {
                MC.player.getInventory().selectedSlot = prevSelectedSlot = enchantableSlot;
                return;
            }
            int emptySlot = InvUtils.findEmptyHotbarSlot();
            if (emptySlot != -1)
                MC.player.getInventory().selectedSlot = prevSelectedSlot = emptySlot;
        }
    }

    public static void tick() {
        if ((!enchanting && !unenchanting) || awaitingResponse || MC.player == null) {
            return;
        }

        if (noPermission) {
            if (ModConfig.HANDLER.instance().enchantMode == ModConfig.EnchantMode.ALL && enchanting) {
                Messenger.printMessage("message.sbutils.enchantAll.noEnchantAllPermission", Formatting.RED);
            } else {
                Messenger.printMessage("message.sbutils.enchantAll.noEnchantPermission", Formatting.RED);
            }
            reset();
            return;
        }

        if (MC.player.getInventory().selectedSlot != prevSelectedSlot) {
            Messenger.printMessage("message.sbutils.enchantAll.cancelSlotSwitch", Formatting.RED);
            reset();
            return;
        }

        if (done()) {
            if (inventory && itemPrevSlot != -1) {
                if (!InvUtils.canSwapSlot(itemPrevSlot))
                    return;
                InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selectedSlot);
            }
            Messenger.printMessage("message.sbutils.enchantAll.complete");
            reset();
            return;
        }

        if (commandCount >= ModConfig.HANDLER.instance().cooldownFrequency) {
            cooldown = true;
            Messenger.printEnchantCooldown(ModConfig.HANDLER.instance().cooldownTime);
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

    public static void processMessage(Text message) {
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

        ItemStack hand = MC.player.getMainHandStack();

        if (!hand.getItem().isEnchantable(hand)) {
            return;
        }

        List<Enchantment> enchants = getEnchantsForItem(MC.player.getMainHandStack(), unenchant);

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

        ItemStack hand = MC.player.getMainHandStack();
        List<Enchantment> enchants = getEnchantsForItem(hand, unenchant);

        if (enchants.size() < 1) {
            if (shouldRemoveFrost()) {
                sendEnchantCommand(Enchantments.FROST_WALKER, true);
                return;
            }
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot != -1) {
                if (itemPrevSlot != -1) {
                    if (!InvUtils.canSwapSlot(itemPrevSlot))
                        return;
                    InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selectedSlot);
                    itemPrevSlot = -1;
                }
                if (!InvUtils.canSwapSlot(enchantableSlot)) {
                    return;
                }
                InvUtils.swapToHotbar(enchantableSlot, MC.player.getInventory().selectedSlot);
                itemPrevSlot = enchantableSlot;
                pause = true;
            }
            return;
        }

        sendNextEnchant(enchants, unenchant);
    }

    private static void sendNextEnchant(List<Enchantment> enchants, boolean unenchant) {
        if (!unenchant && ModConfig.HANDLER.instance().enchantMode == ModConfig.EnchantMode.ALL) {
            sendEnchantAllCommand();
            return;
        }

        sendEnchantCommand(enchants.iterator().next(), unenchant);
    }

    private static void sendEnchantCommand(Enchantment enchantment, boolean unenchant) {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        String enchantName = Registries.ENCHANTMENT.getId(enchantment).getPath().replaceAll("_", "");
        MC.getNetworkHandler().sendChatCommand("enchant " + enchantName + " " + (unenchant ? 0 : enchantment.getMaxLevel()));
        afterSendCommand();
    }

    private static void sendEnchantAllCommand() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        MC.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("enchantall", Instant.now(), NetworkEncryptionUtils.SecureRandomUtil.nextLong(),
                ArgumentSignatureDataMap.EMPTY, new LastSeenMessagesCollector(20).collect().update()));
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

        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            if (i >= 36 && i <= 39) {
                // Skip armor slots
                continue;
            }
            ItemStack itemStack = MC.player.getInventory().getStack(i);
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

        Map<Enchantment, Integer> itemsEnchants = EnchantmentHelper.fromNbt(itemStack.getEnchantments());
        List<Enchantment> enchantments = new ArrayList<>();
        if (!unenchant) {
            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if (enchantment.isAcceptableItem(new ItemStack(item))) {
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

        if (ModConfig.HANDLER.instance().excludeFrost && !unenchant) {
            enchantments.remove(Enchantments.FROST_WALKER);
        }

        return enchantments;
    }

    private static int delayLeft() {
        long delay = (long)(ModConfig.HANDLER.instance().enchantDelay * 1000.0);
        if (cooldown) {
            delay = (long)(ModConfig.HANDLER.instance().cooldownTime * 1000.0);
        } else if (pause) {
            delay = 250L;
        }

        return (int)Math.max(delay - (System.currentTimeMillis() - lastActionPerformedAt), 0L);
    }

    private static boolean done() {
        if (MC.player == null) {
            return true;
        }

        ItemStack hand = MC.player.getMainHandStack();

        return ((inventory && findEnchantableSlot(unenchanting) == -1) ||
                (!inventory && getEnchantsForItem(hand, unenchanting).size() < 1)) &&
                !shouldRemoveFrost();
    }

    private static boolean shouldRemoveFrost() {
        if (MC.player == null) {
            return false;
        }

        return ModConfig.HANDLER.instance().excludeFrost &&
                EnchantmentHelper.fromNbt(MC.player.getMainHandStack().getEnchantments()).containsKey(Enchantments.FROST_WALKER);
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
