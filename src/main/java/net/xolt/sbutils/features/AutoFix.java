package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
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

    public static void init() {
        reset();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoFixNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoFix", () -> ModConfig.HANDLER.instance().autoFix.enabled, (value) -> ModConfig.HANDLER.instance().autoFix.enabled = value)
                    .then(CommandHelper.runnable("info", () -> Messenger.printAutoFixInfo(ModConfig.HANDLER.instance().autoFix.enabled, fixing, findMostDamaged(), delayLeft())))
                    .then(CommandHelper.runnable("reset", () -> {reset(); Messenger.printMessage("message.sbutils.autoFix.reset");}))
                    .then(CommandHelper.genericEnum("mode", "mode", "autoFix.mode", ModConfig.FixMode.class, () -> ModConfig.HANDLER.instance().autoFix.mode, (value) -> ModConfig.HANDLER.instance().autoFix.mode = value))
                    .then(CommandHelper.doubl("percent", "percent", "autoFix.percent", () -> ModConfig.HANDLER.instance().autoFix.percent, (value) -> {ModConfig.HANDLER.instance().autoFix.percent = value; onChangeMaxFixPercent();}))
                    .then(CommandHelper.doubl("delay", "seconds", "autoFix.delay", () -> ModConfig.HANDLER.instance().autoFix.delay, (value) -> ModConfig.HANDLER.instance().autoFix.delay = value))
                    .then(CommandHelper.doubl("retryDelay", "seconds", "autoFix.retryDelay", () -> ModConfig.HANDLER.instance().autoFix.retryDelay, (value) -> ModConfig.HANDLER.instance().autoFix.retryDelay = value))
                    .then(CommandHelper.integer("maxRetries", "retries", "autoFix.maxRetries", () -> ModConfig.HANDLER.instance().autoFix.maxRetries, (value) -> ModConfig.HANDLER.instance().autoFix.maxRetries = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoFixNode));
    }

    public static void tick() {
        if (enabled != ModConfig.HANDLER.instance().autoFix.enabled) {
            enabled = ModConfig.HANDLER.instance().autoFix.enabled;
            reset();
        }

        if (!ModConfig.HANDLER.instance().autoFix.enabled || EnchantAll.active() || MC.player == null) {
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

        if (waitingForResponse && currentTime - lastActionPerformedAt > ModConfig.HANDLER.instance().autoFix.retryDelay * 1000) {
            waitingForResponse = false;
            if (tries > ModConfig.HANDLER.instance().autoFix.maxRetries) {
                Messenger.printWithPlaceholders("message.sbutils.autoFix.maxTriesReached", tries);
                if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND) {
                    returnAndSwapBack();
                }
                ModConfig.HANDLER.instance().autoFix.enabled = false;
                ModConfig.HANDLER.save();
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
        if (!ModConfig.HANDLER.instance().autoFix.enabled || fixing) {
            return;
        }

        findMostDamaged = true;
    }

    public static void onChangeMaxFixPercent() {
        findMostDamaged = true;
    }

    private static void doAutoFix() {
        if (MC.player == null) {
            return;
        }

        if (!fixing) {
            AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
            if (itemPrevSlot != -1 && InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler)) {
                fixing = true;

                if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.ALL) {
                    return;
                }

                prevSelectedSlot = MC.player.getInventory().selected;

                if (itemPrevSlot < 9) {
                    MC.player.getInventory().selected = itemPrevSlot;
                    selectedSlot = itemPrevSlot;
                } else {
                    InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selected, currentScreenHandler);
                    selectedSlot = MC.player.getInventory().selected;
                }

                lastActionPerformedAt = System.currentTimeMillis();
            }
            return;
        }

        if (MC.player.getInventory().selected != findMostDamaged()) {
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

    public static void processMessage(Component message) {
        if (!waitingForResponse) {
            return;
        }

        Matcher fixFailMatcher = RegexFilters.fixFailFilter.matcher(message.getString());
        if (fixFailMatcher.matches()) {
            String minutesText = fixFailMatcher.group(3);
            String secondsText = fixFailMatcher.group(6);
            int minutes = minutesText == null || minutesText.isEmpty() ? 0 : Integer.parseInt(minutesText);
            int seconds = secondsText == null || secondsText.isEmpty() ? 0 : Integer.parseInt(secondsText);
            if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND) {
                returnAndSwapBack();
            }
            reset();
            lastActionPerformedAt = calculateLastCommandSentAt((((long)minutes * 60000) + ((long)seconds * 1000) + 2000));
            return;
        }

        Matcher fixSuccessMatcher = RegexFilters.fixSuccessFilter.matcher(message.getString());
        if (fixSuccessMatcher.matches()) {
            if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND) {
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
        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            if (i >= 36 && i <= 39) {
                // Skip armor slots
                continue;
            }

            ItemStack itemStack = MC.player.getInventory().getItem(i);

            if (!itemStack.isDamageableItem()) {
                continue;
            }

            double maxDamage = itemStack.getMaxDamage();

            if (ModConfig.HANDLER.instance().autoFix.percent > -1 && (maxDamage - (double)itemStack.getDamageValue()) / maxDamage > ModConfig.HANDLER.instance().autoFix.percent) {
                continue;
            }

            if (itemStack.getDamageValue() > mostDamage) {
                result = i;
                mostDamage = itemStack.getDamageValue();
            }
        }
        return result;
    }

    private static void sendFixCommand() {
        if (MC.getConnection() == null) {
            return;
        }
        String command = ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND ? "fix" : "fix all";
        MC.getConnection().sendCommand(command);
    }

    private static void returnAndSwapBack() {
        if (MC.player == null)
            return;

        AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
        if (itemPrevSlot != -1 || !InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler))
            return;

        InvUtils.swapToHotbar(itemPrevSlot, selectedSlot, currentScreenHandler);
        MC.player.getInventory().selected = prevSelectedSlot;
    }

    private static int delay() {
        return (int)(ModConfig.HANDLER.instance().autoFix.delay * 1000.0) + 2000;
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
    }
}
