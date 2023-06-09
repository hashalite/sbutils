package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoAdvert {

    private static final String COMMAND = "autoadvert";
    private static final String ALIAS = "advert";

    private static List<String> prevAdList;
    private static int adIndex;
    private static long lastAdSentAt;
    private static long joinedAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoAdvertNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoAdvert = !ModConfig.INSTANCE.getConfig().autoAdvert;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autoadvert", ModConfig.INSTANCE.getConfig().autoAdvert);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("info")
                        .executes(context -> {
                            Messenger.printAutoAdvertInfo(ModConfig.INSTANCE.getConfig().autoAdvert, ServerDetector.currentServer == null, getUpdatedAdIndex(getAdList()), delayLeft(), userWhitelisted(), ModConfig.INSTANCE.getConfig().advertUseWhitelist);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(ClientCommandManager.literal("sbFile")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.skyblockAdFile", getAdFile());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("filename", StringArgumentType.string())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().skyblockAdFile = StringArgumentType.getString(context, "filename");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.skyblockAdFile", ModConfig.INSTANCE.getConfig().skyblockAdFile);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("ecoFile")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.economyAdFile", getAdFile());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("filename", StringArgumentType.string())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().economyAdFile = StringArgumentType.getString(context, "filename");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.economyAdFile", ModConfig.INSTANCE.getConfig().economyAdFile);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("classicFile")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.classicAdFile", getAdFile());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("filename", StringArgumentType.string())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().classicAdFile = StringArgumentType.getString(context, "filename");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.classicAdFile", ModConfig.INSTANCE.getConfig().classicAdFile);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("ads")
                        .then(ClientCommandManager.literal("list")
                                .executes(context ->
                                        onListCommand()
                                ))
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("advert", StringArgumentType.greedyString())
                                        .executes(context ->
                                                onAddCommand(StringArgumentType.getString(context, "advert"))
                                        )))
                        .then(ClientCommandManager.literal("del")
                                .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                        .executes(context ->
                                                onDelCommand(IntegerArgumentType.getInteger(context, "index"))
                                        )))
                        .then(ClientCommandManager.literal("insert")
                                .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                        .then(ClientCommandManager.argument("advert", StringArgumentType.greedyString())
                                                .executes(context ->
                                                        onInsertCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "advert"))
                                                ))))
                        .then(ClientCommandManager.literal("toggle")
                                .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                        .executes(context ->
                                                onToggleCommand(IntegerArgumentType.getInteger(context, "index"))
                                        ))))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.advertDelay", ModConfig.INSTANCE.getConfig().advertDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advertDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.advertDelay", ModConfig.INSTANCE.getConfig().advertDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("initialDelay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.advertInitialDelay", ModConfig.INSTANCE.getConfig().advertInitialDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advertDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.advertInitialDelay", ModConfig.INSTANCE.getConfig().advertInitialDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("whitelist")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.advertUseWhitelist", ModConfig.INSTANCE.getConfig().advertUseWhitelist);
                            Messenger.printListSetting("message.sbutils.autoAdvert.whitelist", ModConfig.INSTANCE.getConfig().advertWhitelist);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advertUseWhitelist = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.advertUseWhitelist", ModConfig.INSTANCE.getConfig().advertUseWhitelist);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("user", StringArgumentType.string())
                                        .executes(context ->
                                                onAddUserCommand(StringArgumentType.getString(context, "user"))
                                        )))
                        .then(ClientCommandManager.literal("del")
                                .then(ClientCommandManager.argument("user", StringArgumentType.string())
                                        .executes(context ->
                                                onDelUserCommand(StringArgumentType.getString(context, "user"))
                                        ))))
                .then(ClientCommandManager.literal("reset")
                        .executes(context -> {
                            reset();
                            Messenger.printMessage("message.sbutils.autoAdvert.reset");
                            return Command.SINGLE_SUCCESS;
                        })));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoAdvertNode));
    }

    private static int onListCommand() {
        if (ServerDetector.currentServer == null) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return Command.SINGLE_SUCCESS;
        }

        Messenger.printListSetting("message.sbutils.autoAdvert.advertList", formatAdList(getAdList()));
        return Command.SINGLE_SUCCESS;
    }

    private static int onAddCommand(String advert) {
        if (ServerDetector.currentServer == null) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return Command.SINGLE_SUCCESS;
        }
        String adFile = getAdFile();
        List<String> adverts = getAdList();
        adverts.add(advert);
        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.addSuccess", formatAdList(adverts));

        return Command.SINGLE_SUCCESS;
    }

    private static int onDelCommand(int index) {
        if (ServerDetector.currentServer == null) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return Command.SINGLE_SUCCESS;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        adverts.remove(adjustedIndex);
        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.deleteSuccess", formatAdList(adverts));

        return Command.SINGLE_SUCCESS;
    }

    private static int onInsertCommand(int index, String advert) {
        if (ServerDetector.currentServer == null) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return Command.SINGLE_SUCCESS;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        adverts.add(adjustedIndex, advert);
        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.addSuccess", formatAdList(adverts));

        return Command.SINGLE_SUCCESS;
    }

    private static int onToggleCommand(int index) {
        if (ServerDetector.currentServer == null) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return Command.SINGLE_SUCCESS;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        String currentValue = adverts.get(adjustedIndex);
        if (currentValue.startsWith("//")) {
            adverts.set(adjustedIndex, currentValue.substring(2));
        } else {
            adverts.set(adjustedIndex, "//" + currentValue);
        }

        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.toggleSuccess", formatAdList(adverts));
        return Command.SINGLE_SUCCESS;
    }

    private static int onAddUserCommand(String user) {
        List<String> whitelist = new ArrayList<>(ModConfig.INSTANCE.getConfig().advertWhitelist);

        if (whitelist.contains(user)) {
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistAddFail", user);
            return Command.SINGLE_SUCCESS;
        }

        whitelist.add(user);
        ModConfig.INSTANCE.getConfig().advertWhitelist = whitelist;
        ModConfig.INSTANCE.save();
        Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistAddSuccess", user);
        return Command.SINGLE_SUCCESS;
    }

    private static int onDelUserCommand(String user) {
        List<String> whitelist = new ArrayList<>(ModConfig.INSTANCE.getConfig().advertWhitelist);

        if (!whitelist.contains(user)) {
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistDelFail", user);
            return Command.SINGLE_SUCCESS;
        }

        whitelist.remove(user);
        ModConfig.INSTANCE.getConfig().advertWhitelist = whitelist;
        ModConfig.INSTANCE.save();
        Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistDelSuccess", user);
        return Command.SINGLE_SUCCESS;
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().autoAdvert || MC.getNetworkHandler() == null) {
            return;
        }

        if (MC.currentScreen instanceof DownloadingTerrainScreen) {
            joinedAt = System.currentTimeMillis();
        }

        if (ServerDetector.currentServer == null || (ModConfig.INSTANCE.getConfig().advertUseWhitelist && !userWhitelisted())) {
            return;
        }

        if (delayLeft() > 0) {
            return;
        }

        List<String> newAdList = getAdList();
        if (findNextAd(newAdList, -1) == -1) {
            ModConfig.INSTANCE.getConfig().autoAdvert = false;
            ModConfig.INSTANCE.save();
            reset();
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.noAds", getAdFile());
            return;
        }

        adIndex = getUpdatedAdIndex(newAdList);
        prevAdList = newAdList;
        sendAd();
        lastAdSentAt = System.currentTimeMillis();
    }

    public static void onJoinGame() {
        if (!ModConfig.INSTANCE.getConfig().autoAdvert) {
            return;
        }

        joinedAt = System.currentTimeMillis();
    }

    private static int getUpdatedAdIndex(List<String> newAdList) {
        if (newAdList == null || prevAdList == null || prevAdList.size() != newAdList.size()) {
            return findNextAd(newAdList, -1);
        }

        for (int i = 0; i < prevAdList.size(); i++) {
            if (!prevAdList.get(i).equals(newAdList.get(i))) {
                return findNextAd(newAdList, -1);
            }
        }

        return findNextAd(newAdList, adIndex);
    }

    private static int findNextAd(List<String> newAdList, int index) {
        if (newAdList == null) {
            return -1;
        }

        int startIndex = index + 1;
        for (int i = 0; i < newAdList.size(); i++) {
            int currentIndex = (startIndex + i) % newAdList.size();
            if (!newAdList.get(currentIndex).startsWith("//")) {
                return currentIndex;
            }
        }

        return -1;
    }

    private static List<String> getAdList() {
        String adFile = getAdFile();
        if (adFile == null) {
            return new ArrayList<>();
        }

        String adListString = IOHandler.readAdFile(getAdFile());

        if (adListString == null || adListString.length() == 0) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(adListString.split("[\\r\\n]+")));
    }

    private static List<String> formatAdList(List<String> ads) {
        List<String> commentsFormatted = ads.stream().map((ad) -> {
            if (ad.startsWith("//")) {
                ad = ad.substring(2);
                ad = ad.replaceAll("&([0-9a-fk-or])", "");
                return Formatting.FORMATTING_CODE_PREFIX + "7" + Formatting.FORMATTING_CODE_PREFIX + "m" + ad;
            } else {
                return ad;
            }
        }).toList();

        return commentsFormatted.stream().map((ad) -> ad.replaceAll("&([0-9a-fk-or])", Formatting.FORMATTING_CODE_PREFIX + "$1")).toList();
    }

    private static String getAdFile() {
        String adFile;
        if (ServerDetector.currentServer == null) {
            return null;
        } else {
            switch (ServerDetector.currentServer) {
                case ECONOMY:
                    adFile = ModConfig.INSTANCE.getConfig().economyAdFile;
                    break;
                case CLASSIC:
                    adFile = ModConfig.INSTANCE.getConfig().classicAdFile;
                    break;
                default:
                    adFile = ModConfig.INSTANCE.getConfig().skyblockAdFile;
                    break;
            }
        }

        if (!adFile.endsWith(".txt")) {
            return adFile + ".txt";
        }

        return adFile;
    }

    private static boolean userWhitelisted() {
        if (MC.player == null) {
            return false;
        }

        return getWhitelist().contains(MC.player.getGameProfile().getName());
    }

    private static List<String> getWhitelist() {
        return ModConfig.INSTANCE.getConfig().advertWhitelist;
    }

    private static void sendAd() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        MC.getNetworkHandler().sendChatMessage(prevAdList.get(adIndex));
    }

    private static int delayLeft() {
        long delay = (long)(ModConfig.INSTANCE.getConfig().advertDelay * 1000.0);
        long initialDelay = (long)(ModConfig.INSTANCE.getConfig().advertInitialDelay * 1000.0);

        int delayLeft = (int)Math.max(delay - (System.currentTimeMillis() - lastAdSentAt), 0L);
        int initialDelayLeft = (int)Math.max(initialDelay - (System.currentTimeMillis() - joinedAt), 0L);

        return Math.max(delayLeft, initialDelayLeft);
    }

    private static void reset() {
        lastAdSentAt = 0;
        adIndex = 0;
    }

    public static void refreshPrevAdlist() {
        prevAdList = getAdList();
    }
}
