package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.util.*;

import java.util.*;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoKit {
    private static final String COMMAND = "autokit";
    private static final String ALIAS = "ak";
    private static final PriorityQueue<KitQueueEntry> kitQueue = new PriorityQueue<>(KitQueueEntry.KIT_QUEUE_ENTRY_COMPARATOR);
    private static final List<KitQueueEntry> invFullList = new ArrayList<>();

    private static boolean enabled;
    private static Map<String, Map<String, Long>> kitData;
    private static boolean awaitingResponse;
    private static long lastCommandSentAt;
    private static long joinedAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoKitNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoKit", () -> ModConfig.HANDLER.instance().autoKit.enabled, (value) -> ModConfig.HANDLER.instance().autoKit.enabled = value)
                        .then(CommandHelper.doubl("commandDelay", "seconds", "autoKit.commandDelay", () -> ModConfig.HANDLER.instance().autoKit.commandDelay, (value) -> ModConfig.HANDLER.instance().autoKit.commandDelay = value, 0))
                        .then(CommandHelper.doubl("claimDelay", "seconds", "autoKit.claimDelay", () -> ModConfig.HANDLER.instance().autoKit.claimDelay, (value) -> ModConfig.HANDLER.instance().autoKit.claimDelay = value, 0))
                        .then(CommandHelper.doubl("systemDelay", "seconds", "autoKit.systemDelay", () -> ModConfig.HANDLER.instance().autoKit.systemDelay, (value) -> ModConfig.HANDLER.instance().autoKit.systemDelay = value, 0))
                        .then(CommandHelper.enumList("kits", "kit", "autoKit.kits", ModConfig.Kit.class, () -> ModConfig.HANDLER.instance().autoKit.kits, (value) -> ModConfig.HANDLER.instance().autoKit.kits = value))
                        .then(CommandHelper.runnable("info", () -> Messenger.printAutoKitInfo(kitQueue, invFullList)))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoKitNode));
    }

    public static void tick() {
        if (enabled != ModConfig.HANDLER.instance().autoKit.enabled) {
            enabled = ModConfig.HANDLER.instance().autoKit.enabled;
            reset();
            if (enabled && ServerDetector.currentServer == ServerDetector.SbServer.SKYBLOCK) {
                queueKits();
            }
        }

        if (!enabled || awaitingResponse || ServerDetector.currentServer != ServerDetector.SbServer.SKYBLOCK || MC.player == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // 10s delay needed due to chat messages being held for 10 seconds upon joining
        if (currentTime - joinedAt < 10000)
            return;

        // Enforce delay between commands
        if (currentTime - lastCommandSentAt < ModConfig.HANDLER.instance().autoKit.commandDelay * 1000) {
            return;
        }

        KitQueueEntry kit = kitQueue.peek();

        if (kit == null || kit.claimAt > currentTime) {
            return;
        }

        if (InvUtils.doesKitFit(MC.player.getInventory(), kit.kit.getItems())) {
            claimKit(kit.kit);
            return;
        }

        Messenger.printWithPlaceholders("message.sbutils.autoKit.invFull", kit.kit);
        kitQueue.poll();
        invFullList.add(kit);
    }

    public static void init() {
        kitData = IOHandler.readAutoKitData();
        awaitingResponse = false;
        lastCommandSentAt = 0;
    }

    public static void onUpdateInventory() {
        if (!enabled || invFullList.isEmpty() || MC.player == null)
            return;
        List<KitQueueEntry> kitsWithSpace = invFullList.stream().filter((kit) -> InvUtils.doesKitFit(MC.player.getInventory(), kit.kit.getItems())).toList();
        kitQueue.addAll(kitsWithSpace);
        invFullList.removeAll(kitsWithSpace);
    }

    public static void onJoinGame() {
        if (!enabled) {
            return;
        }

        joinedAt = System.currentTimeMillis();
    }

    public static void onSwitchServer(ServerDetector.SbServer server) {
        reset();
        if (server != ServerDetector.SbServer.SKYBLOCK)
            return;
        queueKits();
    }

    public static void onDisconnect() {
        reset();
    }

    public static void processMessage(Component message) {
        if (!enabled || !awaitingResponse || MC.player == null) {
            return;
        }

        String messageString = message.getString();
        Matcher kitSuccessMatcher = RegexFilters.kitSuccessFilter.matcher(messageString);
        Matcher kitFailMatcher = RegexFilters.kitFailFilter.matcher(messageString);
        Matcher kitNoPermsMatcher = RegexFilters.kitNoPermsFilter.matcher(messageString);
        if (!kitSuccessMatcher.matches() && !kitFailMatcher.matches() && !kitNoPermsMatcher.matches())
            return;

        if (kitNoPermsMatcher.matches()) {
            kitQueue.poll();
            // Error message for no perms
            awaitingResponse = false;
            return;
        }

        String player = MC.player.getName().getString();
        if (!kitData.containsKey(player)) {
            kitData.put(player, new HashMap<>());
        }
        KitQueueEntry kitEntry = kitQueue.peek();
        if (kitEntry == null) {
            awaitingResponse = false;
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lastClaimed = 0;
        if (kitSuccessMatcher.matches()) {
            lastClaimed = currentTime;
        } else if (kitFailMatcher.matches()) {
            String daysText = kitFailMatcher.group(3);
            String hoursText = kitFailMatcher.group(6);
            String minutesText = kitFailMatcher.group(9);
            String secondsText = kitFailMatcher.group(12);
            int days = daysText == null || daysText.isEmpty() ? 0 : Integer.parseInt(daysText);
            int hours =  hoursText == null || hoursText.isEmpty() ? 0 : Integer.parseInt(hoursText);
            int minutes =  minutesText == null || minutesText.isEmpty() ? 0 : Integer.parseInt(minutesText);
            int seconds =  secondsText == null || secondsText.isEmpty() ? 0 : Integer.parseInt(secondsText);
            long resetTime = ((long)days * 86400000) + ((long)hours * 3600000) + ((long)minutes * 60000) + ((long)seconds * 1000);
            // 10s buffer needed due to
            lastClaimed = currentTime - (((long)kitEntry.kit.getCooldown() * 3600000) - resetTime);
        }

        kitData.get(player).put(kitEntry.kit.getSerializedName(), lastClaimed);
        IOHandler.writeAutoKitData(kitData);
        kitQueue.poll();
        queueKit(kitEntry.kit);
        awaitingResponse = false;
    }

    private static void claimKit(ModConfig.Kit kit) {
        if (MC.getConnection() == null) {
            return;
        }

        MC.getConnection().sendCommand("kit " + kit.getSerializedName());
        lastCommandSentAt = System.currentTimeMillis();
        awaitingResponse = true;
    }

    public static void onKitListChanged() {
        reset();
        if (enabled)
            queueKits();
    }

    private static void queueKits() {
        kitQueue.clear();
        for (int i = 0; i < ModConfig.HANDLER.instance().autoKit.kits.size(); i++) {
            queueKit(ModConfig.HANDLER.instance().autoKit.kits.get(i));
        }
    }

    private static void queueKit(ModConfig.Kit kit) {
        if (MC.player == null) {
            return;
        }
        String player = MC.player.getName().getString();
        long lastClaimed = 0;
        if (kitData.containsKey(player) && kitData.get(player).containsKey(kit.getSerializedName()))
            lastClaimed = kitData.get(player).get(kit.getSerializedName());

        long cooldownLeft = kitCooldownLeft(kit, lastClaimed);

        kitQueue.add(new KitQueueEntry(kit, System.currentTimeMillis() + cooldownLeft));
    }

    private static long kitCooldownLeft(ModConfig.Kit kit, long lastClaim) {
        long currentTime = System.currentTimeMillis();
        // 24-hour cooldown kits reset at 12:00 AM UTC every day
        if (kit.getCooldown() == 24) {
            long lastReset = currentTime - (currentTime % 86400000);
            // If lastClaim plus buffer is before last reset and more than claimDelay seconds have elapsed since the last reset, return 0
            if (lastClaim + (ModConfig.HANDLER.instance().autoKit.systemDelay * 1000) < lastReset && currentTime > lastReset + (ModConfig.HANDLER.instance().autoKit.systemDelay * 1000)) {
                return 0;
            }
            long nextReset = lastReset + 86400000 + (long)(ModConfig.HANDLER.instance().autoKit.systemDelay * 1000);
            return nextReset - currentTime;
        }
        long nextReset = lastClaim + ((long)kit.getCooldown() * 3600000) + (long)(ModConfig.HANDLER.instance().autoKit.claimDelay * 1000);
        return Math.max(0, nextReset - currentTime);
    }

    private static void reset() {
        kitQueue.clear();
        invFullList.clear();
        awaitingResponse = false;
        lastCommandSentAt = 0;
    }

    public static class KitQueueEntry {
        public static final Comparator<KitQueueEntry> KIT_QUEUE_ENTRY_COMPARATOR = (o1, o2) -> Math.toIntExact(o1.claimAt - o2.claimAt);
        public ModConfig.Kit kit;
        public long claimAt;

        KitQueueEntry(ModConfig.Kit kit, long claimAt) {
            this.kit = kit;
            this.claimAt = claimAt;
        }
    }
}
