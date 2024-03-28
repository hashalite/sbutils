package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

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
        final LiteralCommandNode<FabricClientCommandSource> autoAdvertNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoAdvert", () -> ModConfig.INSTANCE.autoAdvert.enabled, (value) -> ModConfig.INSTANCE.autoAdvert.enabled = value)
                    .then(CommandUtils.runnable("info", () -> Messenger.printAutoAdvertInfo(ModConfig.INSTANCE.autoAdvert.enabled, ServerDetector.isOnSkyblock(), getUpdatedAdIndex(getAdList()), delayLeft(), userWhitelisted(), ModConfig.INSTANCE.autoAdvert.advertUseWhitelist)))
                    .then(CommandUtils.string("sbFile", "filename", "autoAdvert.skyblockAdFile", () -> ModConfig.INSTANCE.autoAdvert.skyblockAdFile, (value) -> ModConfig.INSTANCE.autoAdvert.skyblockAdFile = value))
                    .then(CommandUtils.string("ecoFile", "filename", "autoAdvert.economyAdFile", () -> ModConfig.INSTANCE.autoAdvert.economyAdFile, (value) -> ModConfig.INSTANCE.autoAdvert.economyAdFile = value))
                    .then(CommandUtils.string("classicFile", "filename", "autoAdvert.classicAdFile", () -> ModConfig.INSTANCE.autoAdvert.classicAdFile, (value) -> ModConfig.INSTANCE.autoAdvert.classicAdFile = value))
                    .then(CommandUtils.stringList("ads", "advert", "message.sbutils.autoAdvert.advertList",
                            () -> formatAds(getAdList()),
                            AutoAdvert::onAddCommand,
                            AutoAdvert::onDelCommand,
                            AutoAdvert::onInsertCommand)
                            .then(ClientCommandManager.literal("toggle")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .executes(context ->
                                                    onToggleCommand(IntegerArgumentType.getInteger(context, "index"))
                                            ))))
                    .then(CommandUtils.doubl("delay", "seconds", "autoAdvert.advertDelay", () -> ModConfig.INSTANCE.autoAdvert.advertDelay, (value) -> ModConfig.INSTANCE.autoAdvert.advertDelay = value))
                    .then(CommandUtils.doubl("initialDelay", "seconds", "autoAdvert.advertInitialDelay", () -> ModConfig.INSTANCE.autoAdvert.advertInitialDelay, (value) -> ModConfig.INSTANCE.autoAdvert.advertInitialDelay = value))
                    .then(CommandUtils.stringList("whitelist", "user", "message.sbutils.autoAdvert.whitelist",
                            () -> ModConfig.INSTANCE.autoAdvert.advertWhitelist,
                            AutoAdvert::onAddUserCommand,
                            AutoAdvert::onDelUserCommand,
                            AutoAdvert::onInsertUserCommand))
                    .then(CommandUtils.runnable("reset", () -> {
                        reset();
                        Messenger.printMessage("message.sbutils.autoAdvert.reset");
                    }))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoAdvertNode));
    }

    private static void onAddCommand(String advert) {
        if (!ServerDetector.isOnSkyblock()) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }
        String adFile = getAdFile();
        List<String> adverts = getAdList();
        adverts.add(advert);
        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.addSuccess", formatAds(adverts));
    }

    private static void onDelCommand(int index) {
        if (!ServerDetector.isOnSkyblock()) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return;
        }

        adverts.remove(adjustedIndex);
        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.deleteSuccess", formatAds(adverts));
    }

    private static void onInsertCommand(int index, String advert) {
        if (!ServerDetector.isOnSkyblock()) {
            Messenger.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            Messenger.printMessage("message.sbutils.autoAdvert.invalidIndex");
            return;
        }

        adverts.add(adjustedIndex, advert);
        IOHandler.writeAdverts(adverts, adFile);

        Messenger.printListSetting("message.sbutils.autoAdvert.addSuccess", formatAds(adverts));
    }

    private static int onToggleCommand(int index) {
        if (!ServerDetector.isOnSkyblock()) {
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

        Messenger.printListSetting("message.sbutils.autoAdvert.toggleSuccess", formatAds(adverts));
        return Command.SINGLE_SUCCESS;
    }

    private static void onAddUserCommand(String user) {
        List<String> whitelist = new ArrayList<>(ModConfig.INSTANCE.autoAdvert.advertWhitelist);

        if (whitelist.contains(user)) {
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistAddFail", user);
            return;
        }

        whitelist.add(user);
        ModConfig.INSTANCE.autoAdvert.advertWhitelist = whitelist;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistAddSuccess", user);
    }

    private static void onDelUserCommand(int index) {
        List<String> whitelist = new ArrayList<>(ModConfig.INSTANCE.autoAdvert.advertWhitelist);

        int adjustedIndex = index - 1;
        if (adjustedIndex >= whitelist.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistInvalidIndex", index);
            return;
        }

        String user = whitelist.remove(adjustedIndex);
        ModConfig.INSTANCE.autoAdvert.advertWhitelist = whitelist;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistDelSuccess", user);
    }

    private static void onInsertUserCommand(int index, String user) {
        List<String> whitelist = new ArrayList<>(ModConfig.INSTANCE.autoAdvert.advertWhitelist);

        int adjustedIndex = index - 1;
        if (adjustedIndex > whitelist.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistInvalidIndex", index);
        }

        if (whitelist.contains(user)) {
            Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistAddFail", user);
            return;
        }

        whitelist.add(adjustedIndex, user);
        ModConfig.INSTANCE.autoAdvert.advertWhitelist = whitelist;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoAdvert.whitelistAddSuccess", user);
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.autoAdvert.enabled || MC.getNetworkHandler() == null) {
            return;
        }

        if (MC.currentScreen instanceof DownloadingTerrainScreen) {
            joinedAt = System.currentTimeMillis();
        }

        if (!ServerDetector.isOnSkyblock() || (ModConfig.INSTANCE.autoAdvert.advertUseWhitelist && !userWhitelisted())) {
            return;
        }

        if (delayLeft() > 0) {
            return;
        }

        List<String> newAdList = getAdList();
        if (findNextAd(newAdList, -1) == -1) {
            ModConfig.INSTANCE.autoAdvert.enabled = false;
            ModConfig.HOLDER.save();
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
        if (!ModConfig.INSTANCE.autoAdvert.enabled) {
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

    private static List<MutableText> formatAds(List<String> ads) {
        return ads.stream().map(AutoAdvert::formatAd).toList();
    }

    private static MutableText formatAd(String input) {
        MutableText text = Text.literal("");
        StringBuilder buffer = new StringBuilder();
        Style currentStyle = Style.EMPTY;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar != '&') {
                buffer.append(currentChar);
                continue;
            }

            Style nextStyle = currentStyle;
            if (i + 1 < input.length() && RegexFilters.colorCodeFilter.matcher(input.substring(i, i + 2)).matches()) {
                if (!buffer.isEmpty()) {
                    text = text.append(Text.literal(buffer.toString()).setStyle(currentStyle.withFormatting()));
                    buffer = new StringBuilder();
                }
                char nextChar = input.charAt(i + 1);
                nextStyle = nextStyle.withColor(Formatting.byCode(nextChar));
                i++;
            } else if (i + 13 < input.length() && RegexFilters.rgbFilter.matcher(input.substring(i, i + 14)).matches()) {
                if (!buffer.isEmpty()) {
                    text = text.append(Text.literal(buffer.toString()).setStyle(currentStyle.withFormatting()));
                    buffer = new StringBuilder();
                }
                String rgb = input.substring(i + 3, i + 14).replaceAll("&", "");
                int color = Integer.parseInt(rgb, 16);
                nextStyle = nextStyle.withColor(color);
                i += 13;
            } else {
                buffer.append(currentChar);
                continue;
            }

            if (!buffer.isEmpty()) {
                text = text.append(Text.literal(buffer.toString()).setStyle(currentStyle.withFormatting()));
                buffer = new StringBuilder();
            }

            currentStyle = nextStyle;
        }

        if (!buffer.isEmpty()) {
            text = text.append(Text.literal(buffer.toString()).setStyle(currentStyle.withFormatting()));
        }

        if (input.startsWith("//")) {
            text = Text.literal(text.getString().substring(2)).formatted(Formatting.GRAY).formatted(Formatting.STRIKETHROUGH);
        }

        return text;
    }

    private static String getAdFile() {
        String adFile;
        if (!ServerDetector.isOnSkyblock()) {
            return null;
        } else {
            switch (ServerDetector.currentServer) {
                case SKYBLOCK:
                    adFile = ModConfig.INSTANCE.autoAdvert.skyblockAdFile;
                    break;
                case ECONOMY:
                    adFile = ModConfig.INSTANCE.autoAdvert.economyAdFile;
                    break;
                case CLASSIC:
                    adFile = ModConfig.INSTANCE.autoAdvert.classicAdFile;
                    break;
                default:
                    return null;
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
        return ModConfig.INSTANCE.autoAdvert.advertWhitelist;
    }

    private static void sendAd() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        MC.getNetworkHandler().sendChatMessage(prevAdList.get(adIndex));
    }

    private static int delayLeft() {
        long delay = (long)(ModConfig.INSTANCE.autoAdvert.advertDelay * 1000.0);
        long initialDelay = (long)(ModConfig.INSTANCE.autoAdvert.advertInitialDelay * 1000.0);

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
