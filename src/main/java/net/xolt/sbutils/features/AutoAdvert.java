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
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoAdvert {
    private static List<String> prevAdList;
    private static int adIndex;
    private static long lastAdSentAt;
    private static long joinedAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> autoAdvertNode = dispatcher.register(ClientCommandManager.literal("autoadvert")
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

        dispatcher.register(ClientCommandManager.literal("advert")
                .executes(context ->
                        dispatcher.execute("autoadvert", context.getSource())
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
        if (index - 1 < 0 || index - 1 >= adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        adverts.remove(index - 1);
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
        if (index - 1 < 0 || index - 1 > adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        adverts.add(index - 1, advert);
        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.addSuccess", formatAdList(adverts));

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
        if (!ModConfig.INSTANCE.getConfig().autoAdvert || ServerDetector.currentServer == null || MC.getNetworkHandler() == null) {
            return;
        }

        if (ModConfig.INSTANCE.getConfig().advertUseWhitelist && !userWhitelisted()) {
            return;
        }

        if (MC.currentScreen instanceof ProgressScreen) {
            joinedAt = System.currentTimeMillis();
        }

        if (delayLeft() > 0) {
            return;
        }

        List<String> newAdList = getAdList();
        if (newAdList.size() == 0) {
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
        adIndex = (adIndex + 1) % prevAdList.size();
    }

    private static int getUpdatedAdIndex(List<String> newAdList) {
        if (newAdList == null || prevAdList == null || prevAdList.size() != newAdList.size()) {
            return 0;
        }

        for (int i = 0; i < prevAdList.size(); i++) {
            if (!prevAdList.get(i).equals(newAdList.get(i))) {
                return 0;
            }
        }

        return adIndex;
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
        return ads.stream().map((ad) -> ad.replaceAll("&([0-9a-fk-or])", Formatting.FORMATTING_CODE_PREFIX + "$1")).toList();
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

        return delayLeft + initialDelayLeft;
    }

    private static void reset() {
        lastAdSentAt = 0;
        adIndex = 0;
    }

    public static void refreshPrevAdlist() {
        prevAdList = getAdList();
    }
}
