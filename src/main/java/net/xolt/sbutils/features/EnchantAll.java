package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.xolt.sbutils.SbUtils.MC;

public class EnchantAll {

    private static boolean enchanting;
    private static boolean unenchanting;
    private static boolean inventory;
    private static boolean awaitingResponse;
    private static boolean pause;
    private static boolean cooldown;
    private static int prevSelectedSlot;
    private static int commandCount;
    private static long lastActionPerformedAt;

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> enchantAllNode = dispatcher.register(ClientCommandManager.literal("enchantall")
                .executes(context ->
                        onEnchantAllCommand(false, false)
                )
                .then(ClientCommandManager.literal("inv")
                        .executes(context ->
                                onEnchantAllCommand(false, true)
                        ))
                .then(ClientCommandManager.literal("mode")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.enchantMode", ModConfig.INSTANCE.getConfig().enchantMode);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("mode", ModConfig.EnchantMode.EnchantModeArgumentType.enchantMode())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().enchantMode = ModConfig.EnchantMode.EnchantModeArgumentType.getEnchantMode(context, "mode");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printSetting("text.sbutils.config.option.enchantMode", ModConfig.INSTANCE.getConfig().enchantMode);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.enchantDelay", ModConfig.INSTANCE.getConfig().enchantDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().enchantDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.enchantDelay", ModConfig.INSTANCE.getConfig().enchantDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("cooldownFrequency")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.cooldownFrequency", ModConfig.INSTANCE.getConfig().cooldownFrequency);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("frequency", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().cooldownFrequency = IntegerArgumentType.getInteger(context, "frequency");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.cooldownFrequency", ModConfig.INSTANCE.getConfig().cooldownFrequency);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("cooldownTime")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.cooldownTime", ModConfig.INSTANCE.getConfig().cooldownTime);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("time", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().cooldownTime = DoubleArgumentType.getDouble(context, "time");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.cooldownTime", ModConfig.INSTANCE.getConfig().cooldownTime);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("excludeFrost")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.excludeFrost", ModConfig.INSTANCE.getConfig().excludeFrost);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeFrost = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeFrost", ModConfig.INSTANCE.getConfig().excludeFrost);
                                    return Command.SINGLE_SUCCESS;
                                }))));


        final LiteralCommandNode<FabricClientCommandSource> unenchantAllNode = dispatcher.register(ClientCommandManager.literal("unenchantall")
                .executes(context ->
                        onEnchantAllCommand(true, false)
                )
                .then(ClientCommandManager.literal("inv")
                        .executes(context ->
                                onEnchantAllCommand(true, true)
                        ))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.enchantDelay", ModConfig.INSTANCE.getConfig().enchantDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().enchantDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.enchantDelay", ModConfig.INSTANCE.getConfig().enchantDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("cooldownFrequency")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.cooldownFrequency", ModConfig.INSTANCE.getConfig().cooldownFrequency);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("frequency", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().cooldownFrequency = IntegerArgumentType.getInteger(context, "frequency");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.cooldownFrequency", ModConfig.INSTANCE.getConfig().cooldownFrequency);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("cooldownTime")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.cooldownTime", ModConfig.INSTANCE.getConfig().cooldownTime);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("time", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().cooldownTime = DoubleArgumentType.getDouble(context, "time");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.cooldownTime", ModConfig.INSTANCE.getConfig().cooldownTime);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("excludeFrost")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.excludeFrost", ModConfig.INSTANCE.getConfig().excludeFrost);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeFrost = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeFrost", ModConfig.INSTANCE.getConfig().excludeFrost);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("eall")
                .executes(context ->
                        dispatcher.execute("enchantall", context.getSource())
                )
                .redirect(enchantAllNode));

        dispatcher.register(ClientCommandManager.literal("ueall")
                .executes(context ->
                        dispatcher.execute("unenchantall", context.getSource())
                )
                .redirect(unenchantAllNode));
    }

    private static int onEnchantAllCommand(boolean unenchant, boolean inv) {
        if (MC.player == null) {
            return Command.SINGLE_SUCCESS;
        }

        if (enchanting || unenchanting) {
            Messenger.printMessage("message.sbutils.enchantAll.pleaseWait", Formatting.RED);
            return Command.SINGLE_SUCCESS;
        }

        enchanting = !unenchant;
        unenchanting = unenchant;
        prevSelectedSlot = MC.player.getInventory().selectedSlot;
        inventory = inv;

        return Command.SINGLE_SUCCESS;
    }

    public static void tick() {
        if ((!enchanting && !unenchanting) || awaitingResponse || MC.player == null) {
            return;
        }

        if (MC.player.getInventory().selectedSlot != prevSelectedSlot) {
            Messenger.printMessage("message.sbutils.enchantAll.cancelSlotSwitch", Formatting.RED);
            reset();
            return;
        }

        if (done()) {
            Messenger.printMessage("message.sbutils.enchantAll.complete");
            reset();
            return;
        }

        if (commandCount >= ModConfig.INSTANCE.getConfig().cooldownFrequency) {
            cooldown = true;
            Messenger.printEnchantCooldown(ModConfig.INSTANCE.getConfig().cooldownTime);
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
        if (!enchanting && !unenchanting) {
            return;
        }

        String messageString = message.getString();
        if (RegexFilters.enchantSingleSuccess.matcher(messageString).matches() ||
                RegexFilters.enchantAllSuccess.matcher(messageString).matches() ||
                RegexFilters.unenchantSuccess.matcher(messageString).matches() ||
                RegexFilters.enchantError.matcher(messageString).matches()) {
            awaitingResponse = false;
        }
    }

    private static void doEnchant(boolean unenchant) {
        if (MC.player == null) {
            return;
        }

        ItemStack hand = MC.player.getMainHandStack();

        if (!hand.getItem().isEnchantable(hand)) {
            Messenger.printMessage("message.sbutils.enchantAll.cantEnchantItem");
            reset();
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
                if (!InvUtils.canSwapSlot(enchantableSlot)) {
                    return;
                }
                InvUtils.swapToHotbar(enchantableSlot, MC.player.getInventory().selectedSlot);
                pause = true;
            }
            return;
        }

        sendNextEnchant(enchants, unenchant);
    }

    private static void sendNextEnchant(List<Enchantment> enchants, boolean unenchant) {
        if (!unenchant && ModConfig.INSTANCE.getConfig().enchantMode == ModConfig.EnchantMode.ALL) {
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

        if (ModConfig.INSTANCE.getConfig().excludeFrost && !unenchant) {
            enchantments.remove(Enchantments.FROST_WALKER);
        }

        return enchantments;
    }

    private static int delayLeft() {
        long delay = (long)(ModConfig.INSTANCE.getConfig().enchantDelay * 1000.0);
        if (cooldown) {
            delay = (long)(ModConfig.INSTANCE.getConfig().cooldownTime * 1000.0);
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

        return ModConfig.INSTANCE.getConfig().excludeFrost &&
                EnchantmentHelper.fromNbt(MC.player.getMainHandStack().getEnchantments()).containsKey(Enchantments.FROST_WALKER);
    }

    private static void reset() {
        enchanting = false;
        unenchanting = false;
        inventory = false;
        awaitingResponse = false;
        pause = false;
        cooldown = false;
        prevSelectedSlot = -1;
        commandCount = 0;
    }

    public static boolean active() {
        return enchanting || unenchanting;
    }
}
