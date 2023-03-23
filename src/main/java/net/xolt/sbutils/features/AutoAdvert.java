package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoAdvert {
    private static List<String> prevAdList;
    private static int adIndex;
    private static long lastAdSentAt;
    private static long joinedAt;

    public static void init() {
        prevAdList = getAdList();
    }

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
                            Messenger.printAutoAdvertInfo(ModConfig.INSTANCE.getConfig().autoAdvert, getUpdatedAdIndex(getAdList()), delayLeft());
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(ClientCommandManager.literal("file")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.advertFile", getAdFile());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("filename", StringArgumentType.string())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advertFile = StringArgumentType.getString(context, "filename");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.advertFile", ModConfig.INSTANCE.getConfig().advertFile);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("list")
                        .executes(context -> {
                            Messenger.printListSetting("message.sbutils.autoAdvert.advertList", "message.sbutils.autoAdvert.noAdverts", getAdList());
                            return Command.SINGLE_SUCCESS;
                        }))
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
                            Messenger.printListSetting("message.sbutils.autoAdvert.whitelist", "messages.sbutils.advertWhitelistNoUsers", ModConfig.INSTANCE.getConfig().advertWhitelist);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advertUseWhitelist = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.useWhitelist", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advertUseWhitelist = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.useWhitelist", false);
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

    private static int onAddCommand(String advert) {
        List<String> adverts = getAdList();
        adverts.add(advert);
        IOHandler.writeAdverts(adverts);

        Messenger.printListSetting("message.sbutils.autoAdvert.addSuccess", "", adverts);

        return Command.SINGLE_SUCCESS;
    }

    private static int onDelCommand(int index) {
        List<String> adverts = getAdList();
        if (index - 1 < 0 || index - 1 >= adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        adverts.remove(index - 1);
        IOHandler.writeAdverts(adverts);

        Messenger.printListSetting("message.sbutils.autoAdvert.deleteSuccess", "", adverts);

        return Command.SINGLE_SUCCESS;
    }

    private static int onInsertCommand(int index, String advert) {
        List<String> adverts = getAdList();
        if (index - 1 < 0 || index - 1 > adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        adverts.add(index - 1, advert);
        IOHandler.writeAdverts(adverts);

        Messenger.printListSetting("message.sbutils.autoAdvert.addSuccess", "", adverts);

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

        if (whitelist.size() == 0) {
            Messenger.printMessage("messages.sbutils.advertWhitelistNoUsers");
            return Command.SINGLE_SUCCESS;
        }

        if (!whitelist.contains(user)) {
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistDelFail", user);
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

        if (ModConfig.INSTANCE.getConfig().advertUseWhitelist && !getWhitelist().contains(MC.player.getGameProfile().getName())) {
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
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.noAds", ModConfig.INSTANCE.getConfig().advertFile + ".txt");
            return;
        }
        adIndex = getUpdatedAdIndex(newAdList);
        prevAdList = newAdList;
        sendAd();
        lastAdSentAt = System.currentTimeMillis();
        adIndex = (adIndex + 1) % prevAdList.size();
    }

    public static void onJoinGame() {
        joinedAt = System.currentTimeMillis();
    }

    private static int getUpdatedAdIndex(List<String> newAdList) {
        if (prevAdList.size() != newAdList.size()) {
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
        String adListString = IOHandler.readAdFile(getAdFile());

        if (adListString == null || adListString.length() == 0) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(adListString.split("[\\r\\n]+")));
    }

    private static String getAdFile() {
        String adFile = ModConfig.INSTANCE.getConfig().advertFile;
        if (!adFile.endsWith(".txt")) {
            return adFile + ".txt";
        }
        return adFile;
    }

    private static List<String> getWhitelist() {
        return ModConfig.INSTANCE.getConfig().advertWhitelist;
    }

    private static void sendAd() {
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
}
