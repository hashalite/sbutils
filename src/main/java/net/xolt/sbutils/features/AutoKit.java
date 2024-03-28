package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
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
    private static List<ModConfig.AutoKitConfig.KitEntry> lastKitList;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoKitNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoKit", () -> ModConfig.INSTANCE.autoKit.autoKit, (value) -> ModConfig.INSTANCE.autoKit.autoKit = value)
                        .then(CommandUtils.doubl("commandDelay", "seconds", "autoKit.autoFixCommandDelay", () -> ModConfig.INSTANCE.autoKit.autoKitCommandDelay, (value) -> ModConfig.INSTANCE.autoKit.autoKitCommandDelay = value, 0))
                        .then(CommandUtils.doubl("claimDelay", "seconds", "autoKit.autoFixClaimDelay", () -> ModConfig.INSTANCE.autoKit.autoKitClaimDelay, (value) -> ModConfig.INSTANCE.autoKit.autoKitClaimDelay = value, 0))
                        .then(CommandUtils.doubl("systemDelay", "seconds", "autoKit.systemDelay", () -> ModConfig.INSTANCE.autoKit.autoKitSystemDelay, (value) -> ModConfig.INSTANCE.autoKit.autoKitSystemDelay = value, 0))
                        .then(CommandUtils.list("kits", "kit", "message.sbutils.autoKit.kits", ModConfig.Kit.KitArgumentType.kit(), ModConfig.Kit.KitArgumentType::getKit,
                                () -> ModConfig.INSTANCE.autoKit.autoKits,
                                AutoKit::onAddCommand,
                                AutoKit::onDelCommand,
                                AutoKit::onInsertCommand))
                        .then(CommandUtils.runnable("info", () -> Messenger.printAutoKitInfo(kitQueue, invFullList)))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoKitNode));
    }

    private static void onAddCommand(ModConfig.Kit kit) {
        if (ModConfig.INSTANCE.autoKit.autoKits.contains(new ModConfig.AutoKitConfig.KitEntry(kit))) {
            Messenger.printMessage("message.sbutils.autoKit.duplicateKit");
            return;
        }

        ModConfig.INSTANCE.autoKit.autoKits.add(new ModConfig.AutoKitConfig.KitEntry(kit));
        ModConfig.HOLDER.save();
        Messenger.printListSetting("message.sbutils.autoKit.kitAddSuccess", ModConfig.INSTANCE.autoKit.autoKits);
        onKitListChanged(ModConfig.INSTANCE.autoKit.autoKits);
    }

    private static void onDelCommand(int index) {
        int adjustedIndex = index - 1;
        if (adjustedIndex >= ModConfig.INSTANCE.autoKit.autoKits.size()) {
            Messenger.printMessage("message.sbutils.autoKit.indexOutOfBounds");
            return;
        }

        ModConfig.INSTANCE.autoKit.autoKits.remove(adjustedIndex);
        ModConfig.HOLDER.save();
        Messenger.printListSetting("message.sbutils.autoKit.kitDelSuccess", ModConfig.INSTANCE.autoKit.autoKits);
        onKitListChanged(ModConfig.INSTANCE.autoKit.autoKits);
    }

    private static void onInsertCommand(int index, ModConfig.Kit kit) {
        if (ModConfig.INSTANCE.autoKit.autoKits.contains(new ModConfig.AutoKitConfig.KitEntry(kit))) {
            Messenger.printMessage("message.sbutils.autoKit.duplicateKit");
            return;
        }

        int adjustedIndex = index - 1;
        if (adjustedIndex > ModConfig.INSTANCE.autoKit.autoKits.size()) {
            Messenger.printMessage("message.sbutils.autoKit.indexOutOfBounds");
            return;
        }

        ModConfig.INSTANCE.autoKit.autoKits.add(adjustedIndex, new ModConfig.AutoKitConfig.KitEntry(kit));
        ModConfig.HOLDER.save();
        Messenger.printListSetting("message.sbutils.autoKit.kitAddSuccess", ModConfig.INSTANCE.autoKit.autoKits);
        onKitListChanged(ModConfig.INSTANCE.autoKit.autoKits);
    }

    public static void tick() {
        if (enabled != ModConfig.INSTANCE.autoKit.autoKit) {
            enabled = ModConfig.INSTANCE.autoKit.autoKit;
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
        if (currentTime - lastCommandSentAt < ModConfig.INSTANCE.autoKit.autoKitCommandDelay * 1000) {
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
        reset();
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

    public static void onConfigSave(ModConfig newConfig) {
        newConfig.autoKit.autoKits = new ArrayList<>(new LinkedHashSet<>(newConfig.autoKit.autoKits));

        if (newConfig.autoKit.autoKits.size() != lastKitList.size()) {
            onKitListChanged(newConfig.autoKit.autoKits);
            return;
        }

        for (ModConfig.AutoKitConfig.KitEntry kitEntry : newConfig.autoKit.autoKits) {
            boolean kitIsNew = true;
            for (ModConfig.AutoKitConfig.KitEntry oldKitEntry : lastKitList) {
                if (oldKitEntry.kit == kitEntry.kit) {
                    kitIsNew = false;
                }
            }
            if (!kitIsNew)
                continue;
            onKitListChanged(newConfig.autoKit.autoKits);
            return;
        }

        for (ModConfig.AutoKitConfig.KitEntry oldKitEntry : lastKitList) {
            boolean kitWasRemoved = true;
            for (ModConfig.AutoKitConfig.KitEntry kitEntry : newConfig.autoKit.autoKits) {
                if (oldKitEntry.kit == kitEntry.kit) {
                    kitWasRemoved = false;
                }
            }
            if (!kitWasRemoved)
                continue;
            onKitListChanged(newConfig.autoKit.autoKits);
            return;
        }
    }

    public static void onKitListChanged(List<ModConfig.AutoKitConfig.KitEntry> kits) {
        lastKitList = new ArrayList<>(kits);
        reset();
        if (enabled)
            queueKits();
    }

    public static void onDisconnect() {
        reset();
    }

    public static void processMessage(Text message) {
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

        kitData.get(player).put(kitEntry.kit.asString(), lastClaimed);
        IOHandler.writeAutoKitData(kitData);
        kitQueue.poll();
        queueKit(kitEntry.kit);
        awaitingResponse = false;
    }

    private static void claimKit(ModConfig.Kit kit) {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        MC.getNetworkHandler().sendChatCommand("kit " + kit.asString());
        lastCommandSentAt = System.currentTimeMillis();
        awaitingResponse = true;
    }

    private static void queueKits() {
        kitQueue.clear();
        for (int i = 0; i < ModConfig.INSTANCE.autoKit.autoKits.size(); i++) {
            queueKit(ModConfig.INSTANCE.autoKit.autoKits.get(i).kit);
        }
    }

    private static void queueKit(ModConfig.Kit kit) {
        if (MC.player == null) {
            return;
        }
        String player = MC.player.getName().getString();
        long lastClaimed = 0;
        if (kitData.containsKey(player) && kitData.get(player).containsKey(kit.asString()))
            lastClaimed = kitData.get(player).get(kit.asString());

        long cooldownLeft = kitCooldownLeft(kit, lastClaimed);

        kitQueue.add(new KitQueueEntry(kit, System.currentTimeMillis() + cooldownLeft));
    }

    private static long kitCooldownLeft(ModConfig.Kit kit, long lastClaim) {
        long currentTime = System.currentTimeMillis();
        // 24-hour cooldown kits reset at 12:00 AM UTC every day
        if (kit.getCooldown() == 24) {
            long lastReset = currentTime - (currentTime % 86400000);
            // If lastClaim plus buffer is before last reset and more than claimDelay seconds have elapsed since the last reset, return 0
            if (lastClaim + (ModConfig.INSTANCE.autoKit.autoKitSystemDelay * 1000) < lastReset && currentTime > lastReset + (ModConfig.INSTANCE.autoKit.autoKitSystemDelay * 1000)) {
                return 0;
            }
            long nextReset = lastReset + 86400000 + (long)(ModConfig.INSTANCE.autoKit.autoKitSystemDelay * 1000);
            return nextReset - currentTime;
        }
        long nextReset = lastClaim + ((long)kit.getCooldown() * 3600000) + (long)(ModConfig.INSTANCE.autoKit.autoKitClaimDelay * 1000);
        return Math.max(0, nextReset - currentTime);
    }

    private static void reset() {
        kitQueue.clear();
        invFullList.clear();
        awaitingResponse = false;
        lastCommandSentAt = 0;
        lastKitList = new ArrayList<>(ModConfig.INSTANCE.autoKit.autoKits);
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
