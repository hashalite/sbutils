package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class EnchantAll {
    private static Map<ItemStack, Integer> itemsToEnchant;
    private static ItemStack currentItem;
    private static boolean enchanting;
    private static boolean unenchanting;
    private static boolean sentLastEnchantForItem;
    private static boolean pause;
    private static boolean cooldown;
    private static boolean sendCooldownMessage;
    private static long lastActionPerformedAt;
    private static int selectedSlot;
    private static int enchantIndex;
    private static int commandCounter;

    private static List<Enchantment> enchantments;

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> enchantAllNode = dispatcher.register(ClientCommandManager.literal("enchantall")
                .executes(enchantAll ->
                        onEnchantAllCommand(false)
                )
                .then(ClientCommandManager.literal("inv")
                        .executes(enchantAll ->
                                onEnchantAllInvCommand(false)
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
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeFrost = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeFrost", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeFrost = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeFrost", false);
                                    return Command.SINGLE_SUCCESS;
                                }))));


        final LiteralCommandNode<FabricClientCommandSource> unenchantAllNode = dispatcher.register(ClientCommandManager.literal("unenchantall")
                .executes(enchantAll ->
                        onEnchantAllCommand(true)
                )
                .then(ClientCommandManager.literal("inv")
                        .executes(enchantAll ->
                                onEnchantAllInvCommand(true)
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
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeFrost = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeFrost", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeFrost = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeFrost", false);
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

    public static void tick() {
        if (!enchanting && !unenchanting) {
            return;
        }

        if (commandCounter >= ModConfig.INSTANCE.getConfig().cooldownFrequency) {
            if (!(sentLastEnchantForItem && itemsToEnchant.size() < 2)) {
                cooldown = true;
                sendCooldownMessage = true;
                lastActionPerformedAt = System.currentTimeMillis();
            }
            commandCounter = 0;
        }

        if (sendCooldownMessage && System.currentTimeMillis() - lastActionPerformedAt >= 250) {
            Messenger.printEnchantCooldown(ModConfig.INSTANCE.getConfig().cooldownTime);
            sendCooldownMessage = false;
        }

        if (delayLeft() > 0) {
            return;
        }

        pause = false;
        cooldown = false;

        if (!sentLastEnchantForItem) {
            if (checkForSlotSwitch()) {
                return;
            }

            sendNextEnchant();

            lastActionPerformedAt = System.currentTimeMillis();
            commandCounter++;
            enchantIndex++;

            if (enchantIndex >= enchantments.size()) {
                sentLastEnchantForItem = true;
                pause = true;
            }
        } else {
            if (itemsToEnchant.size() > 1 && checkForSlotSwitch()) {
                return;
            }

            if (currentItem != null) {
                int originalSlot = itemsToEnchant.get(currentItem);

                if (!InvUtils.canSwapSlot(originalSlot)) {
                    return;
                }

                // Move item back to original slot
                InvUtils.swapToHotbar(originalSlot, selectedSlot);
                itemsToEnchant.remove(currentItem);
                currentItem = null;
            }

            if (itemsToEnchant.size() == 0) {
                Messenger.printMessage("message.sbutils.enchantAll.complete");
                reset();
            } else {
                ItemStack newItem = getNextItem();
                int newItemSlot = itemsToEnchant.get(newItem);

                if (!InvUtils.canSwapSlot(newItemSlot)) {
                    return;
                }

                currentItem = newItem;
                sentLastEnchantForItem = false;
                enchantments = getEnchantsForItem(currentItem, unenchanting);
                enchantIndex = 0;
                InvUtils.swapToHotbar(itemsToEnchant.get(newItem), selectedSlot);
                lastActionPerformedAt = System.currentTimeMillis();
            }
        }
    }

    public static int onEnchantAllInvCommand(boolean unenchant) {
        if (enchanting || unenchanting) {
            Messenger.printMessage("message.sbutils.enchantAll.pleaseWait", Formatting.RED);
            return Command.SINGLE_SUCCESS;
        }

        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            if (i >= 36 && i <= 39) {
                // Skip armor slots
                continue;
            }
            ItemStack itemStack = MC.player.getInventory().getStack(i);
            if (getEnchantsForItem(itemStack, unenchant).size() > 0) {
                itemsToEnchant.put(itemStack.copy(), i);
            }
        }

        selectedSlot = MC.player.getInventory().selectedSlot;

        if (itemsToEnchant.size() == 0) {
            Messenger.printMessage("message.sbutils.enchantAll.nothingEnchantable");
            reset();
            return Command.SINGLE_SUCCESS;
        }

        if (!itemsToEnchant.containsValue(MC.player.getInventory().selectedSlot) && !MC.player.getMainHandStack().isEmpty()) {
            int emptySlot = InvUtils.findEmptyHotbarSlot();
            if (emptySlot != -1) {
                MC.player.getInventory().selectedSlot = emptySlot;
            }
        }

        currentItem = getNextItem();
        enchantments = getEnchantsForItem(currentItem, unenchant);
        enchanting = !unenchant;
        unenchanting = unenchant;
        selectedSlot = MC.player.getInventory().selectedSlot;
        InvUtils.swapToHotbar(itemsToEnchant.get(currentItem), selectedSlot);
        lastActionPerformedAt = System.currentTimeMillis();
        pause = true;

        return Command.SINGLE_SUCCESS;
    }

    public static int onEnchantAllCommand(boolean unenchant) {
        if (enchanting || unenchanting) {
            Messenger.printMessage("message.sbutils.enchantAll.pleaseWait", Formatting.RED);
            return Command.SINGLE_SUCCESS;
        }

        reset();
        itemsToEnchant.put(MC.player.getMainHandStack().copy(), MC.player.getInventory().selectedSlot);
        currentItem = getNextItem();
        enchantments = getEnchantsForItem(currentItem, unenchant);
        selectedSlot = MC.player.getInventory().selectedSlot;

        if (enchantments.size() == 0) {
            Messenger.printMessage("message.sbutils.enchantAll.cantEnchantItem");
            reset();
            return Command.SINGLE_SUCCESS;
        }

        enchanting = !unenchant;
        unenchanting = unenchant;

        return Command.SINGLE_SUCCESS;
    }

    public static void onClickSlot(int slotIndex, SlotActionType actionType, int button) {
        if (!enchanting && !unenchanting) {
            return;
        }

        if (itemsToEnchant.containsValue(slotIndex) || (actionType.equals(SlotActionType.SWAP) && itemsToEnchant.containsValue(button))) {
            reset();
            Messenger.printMessage("message.sbutils.enchantAll.cancelInventoryInteract", Formatting.RED);
        }
    }

    private static List<Enchantment> getEnchantsForItem(ItemStack itemStack, boolean unenchant) {
        Item item = itemStack.getItem();

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
        enchantments.remove(Enchantments.SOUL_SPEED);
        enchantments.remove(Enchantments.SWIFT_SNEAK);

        if (ModConfig.INSTANCE.getConfig().excludeFrost && !unenchant) {
            enchantments.remove(Enchantments.FROST_WALKER);
        }

        return enchantments;
    }

    private static void sendNextEnchant() {
        sendEnchantCommand(enchantments.get(enchantIndex), unenchanting);
    }

    private static void sendEnchantCommand(Enchantment enchantment, boolean unenchant) {
        String enchantName = Registries.ENCHANTMENT.getId(enchantment).getPath().replaceAll("_", "");
        MC.getNetworkHandler().sendChatCommand("enchant " + enchantName + " " + (unenchant ? 0 : enchantment.getMaxLevel()));
    }

    private static ItemStack getNextItem() {
        try {
            return itemsToEnchant.keySet().iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private static boolean checkForSlotSwitch() {
        if (MC.player.getInventory().selectedSlot != selectedSlot) {
            Messenger.printMessage("message.sbutils.enchantAll.cancelSlotSwitch", Formatting.RED);
            reset();
            return true;
        }
        return false;
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

    public static boolean active() {
        return enchanting || unenchanting;
    }

    public static void reset() {
        enchanting = false;
        unenchanting = false;
        sentLastEnchantForItem = false;
        pause = false;
        cooldown = false;
        sendCooldownMessage = false;
        selectedSlot = 0;
        enchantIndex = 0;
        commandCounter = 0;
        enchantments = List.of();
        itemsToEnchant = new LinkedHashMap<>();
        currentItem = null;
    }
}
