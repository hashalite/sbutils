package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.systems.ServerDetector;
import net.xolt.sbutils.util.*;

import java.util.*;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;
import static net.xolt.sbutils.SbUtils.SERVER_DETECTOR;

public class AutoKit extends Feature {
    private final OptionBinding<Boolean> enabled = new OptionBinding<>("autoKit.enabled", Boolean.class, (config) -> config.autoKit.enabled, (config, value) -> config.autoKit.enabled = value);
    private final OptionBinding<Double> commandDelay = new OptionBinding<>("autoKit.commandDelay", Double.class, (config) -> config.autoKit.commandDelay, (config, value) -> config.autoKit.commandDelay = value);
    private final OptionBinding<Double> claimDelay = new OptionBinding<>("autoKit.claimDelay", Double.class, (config) -> config.autoKit.claimDelay, (config, value) -> config.autoKit.claimDelay = value);
    private final OptionBinding<Double> systemDelay = new OptionBinding<>("autoKit.systemDelay", Double.class, (config) -> config.autoKit.systemDelay, (config, value) -> config.autoKit.systemDelay = value);
    private final ListOptionBinding<ModConfig.SkyblockKit> sbKits = new ListOptionBinding<>("autoKit.sbKits", ModConfig.SkyblockKit.SKYTITAN, ModConfig.SkyblockKit.class, (config) -> config.autoKit.sbKits, (config, value) -> config.autoKit.sbKits = value);
    private final ListOptionBinding<ModConfig.EconomyKit> ecoKits = new ListOptionBinding<>("autoKit.ecoKits", ModConfig.EconomyKit.HIGHROLLER, ModConfig.EconomyKit.class, (config) -> config.autoKit.ecoKits, (config, value) -> config.autoKit.ecoKits = value);
    private final ListOptionBinding<ModConfig.ClassicKit> classicKits = new ListOptionBinding<>("autoKit.classicKits", ModConfig.ClassicKit.DONOR250, ModConfig.ClassicKit.class, (config) -> config.autoKit.classicKits, (config, value) -> config.autoKit.classicKits = value);
    private final PriorityQueue<KitQueueEntry> kitQueue;
    private final List<KitQueueEntry> invFullList;
    private final Map<String, Map<String, Map<String, Long>>> kitData;

    private boolean awaitingResponse;
    private long lastCommandSentAt;
    private long joinedAt;

    public AutoKit() {
        super("autoKit", "autokit", "ak");
        enabled.addListener(this::onToggle);
        sbKits.addListener(this::onSbKitListChanged);
        ecoKits.addListener(this::onEcoKitListChanged);
        classicKits.addListener(this::onClassicKitListChanged);
        kitQueue = new PriorityQueue<>(KitQueueEntry.KIT_QUEUE_ENTRY_COMPARATOR);
        invFullList = new ArrayList<>();
        kitData = IOHandler.readAutoKitData();
        awaitingResponse = false;
        lastCommandSentAt = 0;
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, commandDelay, claimDelay, systemDelay, sbKits, ecoKits, classicKits);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoKitNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                        .then(CommandHelper.doubl("commandDelay", "seconds", commandDelay))
                        .then(CommandHelper.doubl("claimDelay", "seconds", claimDelay))
                        .then(CommandHelper.doubl("systemDelay", "seconds", systemDelay))
                        .then(CommandHelper.enumList("sbKits", "kit", sbKits))
                        .then(CommandHelper.enumList("ecoKits", "kit", ecoKits))
                        .then(CommandHelper.enumList("classicKits", "kit", classicKits))
                        .then(CommandHelper.runnable("info", () -> ChatUtils.printAutoKitInfo(kitQueue, invFullList)))
        );
        registerAlias(dispatcher, autoKitNode);
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoKit.enabled || awaitingResponse || !SERVER_DETECTOR.isOnSkyblock() || MC.player == null) {
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

        ChatUtils.printWithPlaceholders("message.sbutils.autoKit.invFull", kit.kit);
        kitQueue.poll();
        invFullList.add(kit);
    }

    private void onToggle(Boolean oldValue, Boolean newValue) {
        reset();
        if (newValue && SERVER_DETECTOR.isOnSkyblock())
            queueKits();
    }

    private void onSbKitListChanged(List<ModConfig.SkyblockKit> oldValue, List<ModConfig.SkyblockKit> newValue) {
        if (SERVER_DETECTOR.getCurrentServer() == ServerDetector.SbServer.SKYBLOCK)
            onKitListChanged();
    }

    private void onEcoKitListChanged(List<ModConfig.EconomyKit> oldValue, List<ModConfig.EconomyKit> newValue) {
        if (SERVER_DETECTOR.getCurrentServer() == ServerDetector.SbServer.ECONOMY)
            onKitListChanged();
    }

    private void onClassicKitListChanged(List<ModConfig.ClassicKit> oldValue, List<ModConfig.ClassicKit> newValue) {
        if (SERVER_DETECTOR.getCurrentServer() == ServerDetector.SbServer.CLASSIC)
            onKitListChanged();
    }

    private void onKitListChanged() {
        reset();
        if (ModConfig.HANDLER.instance().autoKit.enabled)
            queueKits();
    }

    public void onUpdateInventory() {
        if (!ModConfig.HANDLER.instance().autoKit.enabled || invFullList.isEmpty() || MC.player == null)
            return;
        List<KitQueueEntry> kitsWithSpace = invFullList.stream().filter((kit) -> InvUtils.doesKitFit(MC.player.getInventory(), kit.kit.getItems())).toList();
        kitQueue.addAll(kitsWithSpace);
        invFullList.removeAll(kitsWithSpace);
    }

    public void onJoinGame() {
        if (!ModConfig.HANDLER.instance().autoKit.enabled)
            return;

        joinedAt = System.currentTimeMillis();
    }

    public void onSwitchServer() {
        reset();
        queueKits();
    }

    public void onDisconnect() {
        reset();
    }

    public void onKitResponse(Component message) {
        if (!ModConfig.HANDLER.instance().autoKit.enabled || !awaitingResponse || !SERVER_DETECTOR.isOnSkyblock() || MC.player == null) {
            return;
        }

        awaitingResponse = false;

        if (message == null) {
            ChatUtils.printMessage("message.sbutils.autoKit.claimFail");
            enabled.set(ModConfig.HANDLER.instance(), false);
            return;
        }

        String messageString = message.getString();
        Matcher kitSuccessMatcher = RegexFilters.kitSuccessFilter.matcher(messageString);
        Matcher kitFailMatcher = RegexFilters.kitFailFilter.matcher(messageString);
        Matcher kitNoPermsMatcher = RegexFilters.kitNoPermsFilter.matcher(messageString);

        if (kitNoPermsMatcher.matches()) {
            kitQueue.poll();
            // Error message for no perms
            awaitingResponse = false;
            return;
        }

        String server = SERVER_DETECTOR.getCurrentServer().getName();
        if (!kitData.containsKey(server))
            kitData.put(server, new HashMap<>());

        Map<String, Map<String, Long>> serverKitData = kitData.get(server);
        String player = MC.player.getName().getString();
        if (!serverKitData.containsKey(player)) {
            serverKitData.put(player, new HashMap<>());
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

        serverKitData.get(player).put(kitEntry.kit.getSerializedName(), lastClaimed);
        IOHandler.writeAutoKitData(kitData);
        kitQueue.poll();
        queueKit(kitEntry.kit);
        awaitingResponse = false;
    }

    private void claimKit(ModConfig.Kit kit) {
        if (MC.getConnection() == null) {
            return;
        }
        SbUtils.COMMAND_SENDER.sendCommand("kit " + kit.getSerializedName(), this::onKitResponse, RegexFilters.kitFailFilter, RegexFilters.kitSuccessFilter, RegexFilters.kitNoPermsFilter);
        lastCommandSentAt = System.currentTimeMillis();
        awaitingResponse = true;
    }

    private void queueKits() {
        if (!SERVER_DETECTOR.isOnSkyblock())
            return;
        kitQueue.clear();
        switch (SERVER_DETECTOR.getCurrentServer()) {
            case SKYBLOCK -> ModConfig.HANDLER.instance().autoKit.sbKits.forEach(this::queueKit);
            case ECONOMY -> ModConfig.HANDLER.instance().autoKit.ecoKits.forEach(this::queueKit);
            case CLASSIC -> ModConfig.HANDLER.instance().autoKit.classicKits.forEach(this::queueKit);
        }
    }

    private void queueKit(ModConfig.Kit kit) {
        if (MC.player == null) {
            return;
        }
        String server = SERVER_DETECTOR.getCurrentServer().getName();
        String player = MC.player.getName().getString();
        long lastClaimed = 0;
        if (kitData.containsKey(server) && kitData.get(server).containsKey(player) && kitData.get(server).get(player).containsKey(kit.getSerializedName()))
            lastClaimed = kitData.get(server).get(player).get(kit.getSerializedName());

        long cooldownLeft = kitCooldownLeft(kit, lastClaimed);

        kitQueue.add(new KitQueueEntry(kit, System.currentTimeMillis() + cooldownLeft));
    }

    private void reset() {
        kitQueue.clear();
        invFullList.clear();
        awaitingResponse = false;
        lastCommandSentAt = 0;
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
