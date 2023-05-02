package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoFix {

    private static boolean enabled;
    private static boolean fixing;
    private static boolean waitingForResponse;
    private static boolean findMostDamaged;
    private static boolean returnAndSwapBack;
    private static boolean disable;
    private static long lastActionPerformedAt;
    private static int itemPrevSlot;
    private static int prevSelectedSlot;
    private static int selectedSlot;
    private static int tries;

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> autoFixNode = dispatcher.register(ClientCommandManager.literal("autofix")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoFix = !ModConfig.INSTANCE.getConfig().autoFix;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autofix", ModConfig.INSTANCE.getConfig().autoFix);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("info")
                        .executes(context -> {
                            Messenger.printAutoFixInfo(ModConfig.INSTANCE.getConfig().autoFix, fixing, findMostDamaged(), delayLeft());
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(ClientCommandManager.literal("reset")
                        .executes(context -> {
                            Messenger.printMessage("message.sbutils.autoFix.reset");
                            reset();
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(ClientCommandManager.literal("mode")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoFixMode", ModConfig.INSTANCE.getConfig().autoFixMode);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("mode", ModConfig.FixMode.FixModeArgumentType.fixMode())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoFixMode = ModConfig.FixMode.FixModeArgumentType.getFixMode(context, "mode");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoFixMode", ModConfig.INSTANCE.getConfig().autoFixMode);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("percent")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.maxFixPercent", ModConfig.INSTANCE.getConfig().maxFixPercent);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("percent", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().maxFixPercent = DoubleArgumentType.getDouble(context, "percent");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.maxFixPercent", ModConfig.INSTANCE.getConfig().maxFixPercent);
                                    findMostDamaged = true;
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoFixDelay", ModConfig.INSTANCE.getConfig().autoFixDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("delay", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoFixDelay = DoubleArgumentType.getDouble(context, "delay");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoFixDelay", ModConfig.INSTANCE.getConfig().autoFixDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("retryDelay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.fixRetryDelay", ModConfig.INSTANCE.getConfig().fixRetryDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("delay", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().fixRetryDelay = DoubleArgumentType.getDouble(context, "delay");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.fixRetryDelay", ModConfig.INSTANCE.getConfig().fixRetryDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("maxRetries")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.maxFixRetries", ModConfig.INSTANCE.getConfig().maxFixRetries);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("retries", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().maxFixRetries = IntegerArgumentType.getInteger(context, "retries");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.maxFixRetries", ModConfig.INSTANCE.getConfig().maxFixRetries);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("af")
                .executes(context ->
                        dispatcher.execute("autofix", context.getSource())
                )
                .redirect(autoFixNode));
    }

    public static void tick() {
        if (enabled != ModConfig.INSTANCE.getConfig().autoFix) {
            enabled = ModConfig.INSTANCE.getConfig().autoFix;
            reset();
        }

        if (!ModConfig.INSTANCE.getConfig().autoFix || EnchantAll.active() || MC.player == null || MC.currentScreen instanceof ProgressScreen) {
            return;
        }

        if (findMostDamaged && !fixing) {
            itemPrevSlot = findMostDamaged();
            findMostDamaged = false;
        }

        if (waitingForResponse && System.currentTimeMillis() - lastActionPerformedAt > ModConfig.INSTANCE.getConfig().fixRetryDelay * 1000) {
            waitingForResponse = false;
            if (tries > ModConfig.INSTANCE.getConfig().maxFixRetries) {
                disable = true;
                if (ModConfig.INSTANCE.getConfig().autoFixMode == ModConfig.FixMode.HAND) {
                    returnAndSwapBack = true;
                }
                Messenger.printWithPlaceholders("message.sbutils.autoFix.maxTriesReached", tries);
            }
        }

        if (waitingForResponse) {
            return;
        }

        if (delayLeft() > 0) {
            return;
        }

        if (disable) {
            ModConfig.INSTANCE.getConfig().autoFix = false;
            ModConfig.INSTANCE.save();
            reset();
        }

        if (returnAndSwapBack) {
            if (InvUtils.canSwapSlot(itemPrevSlot)) {
                returnAndSwapBack();
            }
            fixing = false;
            returnAndSwapBack = false;
            return;
        }

        if (!fixing) {
            if (itemPrevSlot != -1 && InvUtils.canSwapSlot(itemPrevSlot)) {
                fixing = true;

                if (ModConfig.INSTANCE.getConfig().autoFixMode == ModConfig.FixMode.ALL) {
                    return;
                }

                prevSelectedSlot = MC.player.getInventory().selectedSlot;

                if (itemPrevSlot < 9) {
                    MC.player.getInventory().selectedSlot = itemPrevSlot;
                    selectedSlot = itemPrevSlot;
                } else {
                    InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selectedSlot);
                    selectedSlot = MC.player.getInventory().selectedSlot;
                }

                lastActionPerformedAt = System.currentTimeMillis();
            }
        } else {
            if (MC.player.getInventory().selectedSlot != findMostDamaged()) {
                reset();
                return;
            }

            if (tries == 0) {
                Messenger.printMessage("message.sbutils.autoFix.fixingItem");
            }

            sendFixCommand();

            tries++;
            lastActionPerformedAt = System.currentTimeMillis();
            waitingForResponse = true;
        }
    }

    public static void onDisconnect() {
        reset();
    }

    public static void onUpdateInventory() {
        if (!ModConfig.INSTANCE.getConfig().autoFix || fixing) {
            return;
        }

        findMostDamaged = true;
    }

    public static void processMessage(Text message) {
        if (!waitingForResponse) {
            return;
        }

        Matcher fixFailMatcher = RegexFilters.fixFailFilter.matcher(message.getString());
        if (fixFailMatcher.matches()) {
            String minutesText = fixFailMatcher.group(3);
            String secondsText = fixFailMatcher.group(6);
            int minutes = minutesText.length() > 0 ? Integer.parseInt(minutesText) : 0;
            int seconds = secondsText.length() > 0 ? Integer.parseInt(secondsText) : 0;
            lastActionPerformedAt = calculateLastCommandSentAt((((long)minutes * 60000) + ((long)seconds * 1000) + 2000));
            if (ModConfig.INSTANCE.getConfig().autoFixMode == ModConfig.FixMode.HAND) {
                returnAndSwapBack = true;
            }
            waitingForResponse = false;
            tries = 0;
            return;
        }

        Matcher fixSuccessMatcher = RegexFilters.fixSuccessFilter.matcher(message.getString());
        if (fixSuccessMatcher.matches()) {
            lastActionPerformedAt = System.currentTimeMillis();
            if (ModConfig.INSTANCE.getConfig().autoFixMode == ModConfig.FixMode.HAND) {
                returnAndSwapBack = true;
            }
            waitingForResponse = false;
            tries = 0;
        }
    }

    private static int findMostDamaged() {
        if (MC.player == null) {
            return -1;
        }

        int result = -1;
        int mostDamage = 0;
        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            if (i >= 36 && i <= 39) {
                // Skip armor slots
                continue;
            }

            ItemStack itemStack = MC.player.getInventory().getStack(i);

            if (!itemStack.isDamageable()) {
                continue;
            }

            double maxDamage = itemStack.getMaxDamage();

            if (ModConfig.INSTANCE.getConfig().maxFixPercent > -1 && (maxDamage - (double)itemStack.getDamage()) / maxDamage > ModConfig.INSTANCE.getConfig().maxFixPercent) {
                continue;
            }

            if (itemStack.getDamage() > mostDamage) {
                result = i;
                mostDamage = itemStack.getDamage();
            }
        }
        return result;
    }

    private static void sendFixCommand() {
        if (MC.getNetworkHandler() == null) {
            return;
        }
        String command = ModConfig.INSTANCE.getConfig().autoFixMode == ModConfig.FixMode.HAND ? "fix" : "fix all";
        MC.getNetworkHandler().sendChatCommand(command);
    }

    private static void returnAndSwapBack() {
        if (MC.player == null && itemPrevSlot != -1) {
            return;
        }

        InvUtils.swapToHotbar(itemPrevSlot, selectedSlot);
        MC.player.getInventory().selectedSlot = prevSelectedSlot;
    }

    private static int delay() {
        return (int)(ModConfig.INSTANCE.getConfig().autoFixDelay * 1000.0) + 2000;
    }

    private static int delayLeft() {
        long delay = fixing ? 250L : delay();
        return (int)Math.max((delay - (System.currentTimeMillis() - lastActionPerformedAt)), 0L);
    }

    private static long calculateLastCommandSentAt(long timeLeft) {
        long timeElapsed = (delay() + 2000) - timeLeft;
        return System.currentTimeMillis() - timeElapsed;
    }

    public static boolean fixing() {
        return fixing;
    }

    private static void reset() {
        fixing = false;
        waitingForResponse = false;
        findMostDamaged = true;
        returnAndSwapBack = false;
        disable = false;
        lastActionPerformedAt = 0;
        itemPrevSlot = -1;
        prevSelectedSlot = 0;
        selectedSlot = 0;
        tries = 0;
    }
}
