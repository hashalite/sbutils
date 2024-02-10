package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.xolt.sbutils.command.CommandHelper;
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
                CommandHelper.toggle(COMMAND, "autoAdvert", () -> ModConfig.HANDLER.instance().autoAdvert.enabled, (value) -> ModConfig.HANDLER.instance().autoAdvert.enabled = value)
                    .then(CommandHelper.runnable("info", () -> Messenger.printAutoAdvertInfo(ModConfig.HANDLER.instance().autoAdvert.enabled, ServerDetector.isOnSkyblock(), getUpdatedAdIndex(getAdList()), delayLeft(), userWhitelisted(), ModConfig.HANDLER.instance().autoAdvert.useWhitelist)))
                    .then(CommandHelper.string("sbFile", "filename", "autoAdvert.sbFile", () -> ModConfig.HANDLER.instance().autoAdvert.sbFile, (value) -> ModConfig.HANDLER.instance().autoAdvert.sbFile = value))
                    .then(CommandHelper.string("ecoFile", "filename", "autoAdvert.ecoFile", () -> ModConfig.HANDLER.instance().autoAdvert.ecoFile, (value) -> ModConfig.HANDLER.instance().autoAdvert.ecoFile = value))
                    .then(CommandHelper.string("classicFile", "filename", "autoAdvert.classicFile", () -> ModConfig.HANDLER.instance().autoAdvert.classicFile, (value) -> ModConfig.HANDLER.instance().autoAdvert.classicFile = value))
                    .then(CommandHelper.customIndexedList("ads", "advert", "autoAdvert.adList",
                            StringArgumentType.greedyString(),
                            StringArgumentType::getString,
                            () -> formatAds(getAdList()),
                            AutoAdvert::onAddCommand,
                            AutoAdvert::onDelCommand,
                            AutoAdvert::onInsertCommand)
                            .then(ClientCommandManager.literal("toggle")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .executes(context ->
                                                    onToggleCommand(IntegerArgumentType.getInteger(context, "index"))
                                            ))))
                    .then(CommandHelper.doubl("delay", "seconds", "autoAdvert.delay", () -> ModConfig.HANDLER.instance().autoAdvert.delay, (value) -> ModConfig.HANDLER.instance().autoAdvert.delay = value))
                    .then(CommandHelper.doubl("initialDelay", "seconds", "autoAdvert.initialDelay", () -> ModConfig.HANDLER.instance().autoAdvert.initialDelay, (value) -> ModConfig.HANDLER.instance().autoAdvert.initialDelay = value))
                    .then(CommandHelper.stringList("whitelist", "user", "autoAdvert.whitelist", () -> ModConfig.HANDLER.instance().autoAdvert.whitelist)
                            .then(CommandHelper.bool("enabled", "autoAdvert.useWhitelist", () -> ModConfig.HANDLER.instance().autoAdvert.useWhitelist, (value) -> ModConfig.HANDLER.instance().autoAdvert.useWhitelist = value)))
                    .then(CommandHelper.runnable("reset", () -> {
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

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoAdvert.enabled || MC.getNetworkHandler() == null) {
            return;
        }

        if (MC.currentScreen instanceof DownloadingTerrainScreen) {
            joinedAt = System.currentTimeMillis();
        }

        if (!ServerDetector.isOnSkyblock() || (ModConfig.HANDLER.instance().autoAdvert.useWhitelist && !userWhitelisted())) {
            return;
        }

        if (delayLeft() > 0) {
            return;
        }

        List<String> newAdList = getAdList();
        if (findNextAd(newAdList, -1) == -1) {
            ModConfig.HANDLER.instance().autoAdvert.enabled = false;
            ModConfig.HANDLER.save();
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
        if (!ModConfig.HANDLER.instance().autoAdvert.enabled) {
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
                    adFile = ModConfig.HANDLER.instance().autoAdvert.sbFile;
                    break;
                case ECONOMY:
                    adFile = ModConfig.HANDLER.instance().autoAdvert.ecoFile;
                    break;
                case CLASSIC:
                    adFile = ModConfig.HANDLER.instance().autoAdvert.classicFile;
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
        return ModConfig.HANDLER.instance().autoAdvert.whitelist;
    }

    private static void sendAd() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        MC.getNetworkHandler().sendChatMessage(prevAdList.get(adIndex));
    }

    private static int delayLeft() {
        long delay = (long)(ModConfig.HANDLER.instance().autoAdvert.delay * 1000.0);
        long initialDelay = (long)(ModConfig.HANDLER.instance().autoAdvert.initialDelay * 1000.0);

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
