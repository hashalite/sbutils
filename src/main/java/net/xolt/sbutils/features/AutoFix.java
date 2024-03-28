package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoFix {

    private static final String COMMAND = "autofix";
    private static final String ALIAS = "af";

    private static boolean enabled;
    private static boolean fixing;
    private static boolean waitingForResponse;
    private static boolean findMostDamaged;
    private static long lastActionPerformedAt;
    private static int itemPrevSlot;
    private static int prevSelectedSlot;
    private static int selectedSlot;
    private static int tries;
    private static long joinedAt;
    private static double lastMaxFixPercent;

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoFixNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoFix", () -> ModConfig.INSTANCE.autoFix.autoFix, (value) -> ModConfig.INSTANCE.autoFix.autoFix = value)
                    .then(CommandUtils.runnable("info", () -> Messenger.printAutoFixInfo(ModConfig.INSTANCE.autoFix.autoFix, fixing, findMostDamaged(), delayLeft())))
                    .then(CommandUtils.runnable("reset", () -> {reset(); Messenger.printMessage("message.sbutils.autoFix.reset");}))
                    .then(CommandUtils.getterSetter("mode", "mode", "autoFix.autoFixMode", () -> ModConfig.INSTANCE.autoFix.autoFixMode, (value) -> ModConfig.INSTANCE.autoFix.autoFixMode = value, ModConfig.FixMode.FixModeArgumentType.fixMode(), ModConfig.FixMode.FixModeArgumentType::getFixMode))
                    .then(CommandUtils.doubl("percent", "percent", "autoFix.maxFixPercent", () -> ModConfig.INSTANCE.autoFix.maxFixPercent, (value) -> {ModConfig.INSTANCE.autoFix.maxFixPercent = value; onChangeMaxFixPercent(value);}))
                    .then(CommandUtils.doubl("delay", "seconds", "autoFix.autoFixDelay", () -> ModConfig.INSTANCE.autoFix.autoFixDelay, (value) -> ModConfig.INSTANCE.autoFix.autoFixDelay = value))
                    .then(CommandUtils.doubl("retryDelay", "seconds", "autoFix.fixRetryDelay", () -> ModConfig.INSTANCE.autoFix.fixRetryDelay, (value) -> ModConfig.INSTANCE.autoFix.fixRetryDelay = value))
                    .then(CommandUtils.integer("maxRetries", "retries", "autoFix.maxFixRetries", () -> ModConfig.INSTANCE.autoFix.maxFixRetries, (value) -> ModConfig.INSTANCE.autoFix.maxFixRetries = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoFixNode));
    }

    public static void tick() {
        if (enabled != ModConfig.INSTANCE.autoFix.autoFix) {
            enabled = ModConfig.INSTANCE.autoFix.autoFix;
            reset();
        }

        if (!ModConfig.INSTANCE.autoFix.autoFix || EnchantAll.active() || MC.player == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // 10s delay needed due to chat messages being held for 10 seconds upon joining
        if (currentTime - joinedAt < 10000)
            return;

        if (findMostDamaged && !fixing) {
            itemPrevSlot = findMostDamaged();
            findMostDamaged = false;
        }

        if (waitingForResponse && currentTime - lastActionPerformedAt > ModConfig.INSTANCE.autoFix.fixRetryDelay * 1000) {
            waitingForResponse = false;
            if (tries > ModConfig.INSTANCE.autoFix.maxFixRetries) {
                Messenger.printWithPlaceholders("message.sbutils.autoFix.maxTriesReached", tries);
                if (ModConfig.INSTANCE.autoFix.autoFixMode == ModConfig.FixMode.HAND) {
                    returnAndSwapBack();
                }
                ModConfig.INSTANCE.autoFix.autoFix = false;
                ModConfig.HOLDER.save();
                reset();
            }
        }

        if (waitingForResponse) {
            return;
        }

        if (delayLeft() > 0) {
            return;
        }

        doAutoFix();
    }

    public static void onJoinGame() {
        joinedAt = System.currentTimeMillis();
    }

    public static void onDisconnect() {
        reset();
    }

    public static void onUpdateInventory() {
        if (!ModConfig.INSTANCE.autoFix.autoFix || fixing) {
            return;
        }

        findMostDamaged = true;
    }

    public static void onConfigSave(ModConfig newConfig) {
        double maxFixPercent = newConfig.autoFix.maxFixPercent;
        if (maxFixPercent != lastMaxFixPercent) {
            onChangeMaxFixPercent(maxFixPercent);
        }
    }

    private static void onChangeMaxFixPercent(double maxFixPercent) {
        findMostDamaged = true;
        lastMaxFixPercent = maxFixPercent;
    }

    private static void doAutoFix() {
        if (!fixing) {
            if (itemPrevSlot != -1 && InvUtils.canSwapSlot(itemPrevSlot)) {
                fixing = true;

                if (ModConfig.INSTANCE.autoFix.autoFixMode == ModConfig.FixMode.ALL) {
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

    public static void processMessage(Text message) {
        if (!waitingForResponse) {
            return;
        }

        Matcher fixFailMatcher = RegexFilters.fixFailFilter.matcher(message.getString());
        if (fixFailMatcher.matches()) {
            String minutesText = fixFailMatcher.group(3);
            String secondsText = fixFailMatcher.group(6);
            int minutes = minutesText == null || minutesText.isEmpty() ? 0 : Integer.parseInt(minutesText);
            int seconds = secondsText == null || secondsText.isEmpty() ? 0 : Integer.parseInt(secondsText);
            if (ModConfig.INSTANCE.autoFix.autoFixMode == ModConfig.FixMode.HAND) {
                returnAndSwapBack();
            }
            reset();
            lastActionPerformedAt = calculateLastCommandSentAt((((long)minutes * 60000) + ((long)seconds * 1000) + 2000));
            return;
        }

        Matcher fixSuccessMatcher = RegexFilters.fixSuccessFilter.matcher(message.getString());
        if (fixSuccessMatcher.matches()) {
            if (ModConfig.INSTANCE.autoFix.autoFixMode == ModConfig.FixMode.HAND) {
                returnAndSwapBack();
            }
            reset();
            lastActionPerformedAt = System.currentTimeMillis();
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

            if (ModConfig.INSTANCE.autoFix.maxFixPercent > -1 && (maxDamage - (double)itemStack.getDamage()) / maxDamage > ModConfig.INSTANCE.autoFix.maxFixPercent) {
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
        String command = ModConfig.INSTANCE.autoFix.autoFixMode == ModConfig.FixMode.HAND ? "fix" : "fix all";
        MC.getNetworkHandler().sendChatCommand(command);
    }

    private static void returnAndSwapBack() {
        if (MC.player == null && itemPrevSlot != -1 || !InvUtils.canSwapSlot(itemPrevSlot)) {
            return;
        }

        InvUtils.swapToHotbar(itemPrevSlot, selectedSlot);
        MC.player.getInventory().selectedSlot = prevSelectedSlot;
    }

    private static int delay() {
        return (int)(ModConfig.INSTANCE.autoFix.autoFixDelay * 1000.0) + 2000;
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
        lastActionPerformedAt = 0;
        itemPrevSlot = -1;
        prevSelectedSlot = 0;
        selectedSlot = 0;
        tries = 0;
        lastMaxFixPercent = ModConfig.INSTANCE.autoFix.maxFixPercent;
    }
}
