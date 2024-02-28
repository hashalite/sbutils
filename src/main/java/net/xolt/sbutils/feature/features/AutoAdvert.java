package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.systems.ServerDetector;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoAdvert extends Feature {

    private static final String COMMAND = "autoadvert";
    private static final String ALIAS = "advert";

    private List<String> prevAdList;
    private int adIndex;
    private long lastAdSentAt;
    private long joinedAt;

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoAdvertNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoAdvert", () -> ModConfig.HANDLER.instance().autoAdvert.enabled, (value) -> ModConfig.HANDLER.instance().autoAdvert.enabled = value)
                    .then(CommandHelper.runnable("info", () -> ChatUtils.printAutoAdvertInfo(ModConfig.HANDLER.instance().autoAdvert.enabled, SbUtils.SERVER_DETECTOR.isOnSkyblock(), getUpdatedAdIndex(getAdList(), prevAdList, adIndex), delayLeft(), userWhitelisted(), ModConfig.HANDLER.instance().autoAdvert.useWhitelist)))
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
                    .then(CommandHelper.stringList("whitelist", "user", "autoAdvert.whitelist", false, () -> ModConfig.HANDLER.instance().autoAdvert.whitelist, (value) -> ModConfig.HANDLER.instance().autoAdvert.whitelist = value)
                            .then(CommandHelper.bool("enabled", "autoAdvert.useWhitelist", () -> ModConfig.HANDLER.instance().autoAdvert.useWhitelist, (value) -> ModConfig.HANDLER.instance().autoAdvert.useWhitelist = value)))
                    .then(CommandHelper.runnable("reset", () -> {
                        reset();
                        ChatUtils.printMessage("message.sbutils.autoAdvert.reset");
                    }))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoAdvertNode));
    }

    private static void onAddCommand(String advert) {
        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock()) {
            ChatUtils.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }
        String adFile = getAdFile();
        List<String> adverts = getAdList();
        adverts.add(advert);
        IOHandler.writeAdverts(adverts, adFile);

        ChatUtils.printListSetting("message.sbutils.autoAdvert.addSuccess", formatAds(adverts));
    }

    private static void onDelCommand(int index) {
        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock()) {
            ChatUtils.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.autoAdvert.adList"));
            return;
        }

        adverts.remove(adjustedIndex);
        IOHandler.writeAdverts(adverts, adFile);

        ChatUtils.printListSetting("message.sbutils.autoAdvert.deleteSuccess", formatAds(adverts));
    }

    private static void onInsertCommand(int index, String advert) {
        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock()) {
            ChatUtils.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.autoAdvert.adList"));
            return;
        }

        adverts.add(adjustedIndex, advert);
        IOHandler.writeAdverts(adverts, adFile);

        ChatUtils.printListSetting("message.sbutils.autoAdvert.addSuccess", formatAds(adverts));
    }

    private static int onToggleCommand(int index) {
        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock()) {
            ChatUtils.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return Command.SINGLE_SUCCESS;
        }

        String adFile = getAdFile();
        List<String> adverts = getAdList();

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= adverts.size()) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.autoAdvert.adList"));
            return Command.SINGLE_SUCCESS;
        }

        String currentValue = adverts.get(adjustedIndex);
        if (currentValue.startsWith("//")) {
            adverts.set(adjustedIndex, currentValue.substring(2));
        } else {
            adverts.set(adjustedIndex, "//" + currentValue);
        }

        IOHandler.writeAdverts(adverts, adFile);

        ChatUtils.printListSetting("message.sbutils.autoAdvert.toggleSuccess", formatAds(adverts));
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoAdvert.enabled || MC.getConnection() == null) {
            return;
        }

        if (MC.screen instanceof ReceivingLevelScreen) {
            joinedAt = System.currentTimeMillis();
        }

        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock() || (ModConfig.HANDLER.instance().autoAdvert.useWhitelist && !userWhitelisted())) {
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
            ChatUtils.printWithPlaceholders("message.sbutils.autoAdvert.noAds", getAdFile());
            return;
        }

        adIndex = getUpdatedAdIndex(newAdList, prevAdList, adIndex);
        prevAdList = newAdList;
        sendAd();
        lastAdSentAt = System.currentTimeMillis();
    }

    public void onJoinGame() {
        if (!ModConfig.HANDLER.instance().autoAdvert.enabled) {
            return;
        }

        joinedAt = System.currentTimeMillis();
    }

    public void onSwitchServer() {
        refreshPrevAdlist();
    }

    private void sendAd() {
        if (MC.getConnection() == null) {
            return;
        }

        MC.getConnection().sendChat(prevAdList.get(adIndex));
    }

    private int delayLeft() {
        long delay = (long)(ModConfig.HANDLER.instance().autoAdvert.delay * 1000.0);
        long initialDelay = (long)(ModConfig.HANDLER.instance().autoAdvert.initialDelay * 1000.0);

        int delayLeft = (int)Math.max(delay - (System.currentTimeMillis() - lastAdSentAt), 0L);
        int initialDelayLeft = (int)Math.max(initialDelay - (System.currentTimeMillis() - joinedAt), 0L);

        return Math.max(delayLeft, initialDelayLeft);
    }

    private void refreshPrevAdlist() {
        prevAdList = getAdList();
    }

    private void reset() {
        lastAdSentAt = 0;
        adIndex = 0;
    }

    private static int getUpdatedAdIndex(List<String> newAdList, List<String> prevAdList, int curAdIndex) {
        if (newAdList == null || prevAdList == null || prevAdList.size() != newAdList.size()) {
            return findNextAd(newAdList, -1);
        }

        for (int i = 0; i < prevAdList.size(); i++) {
            if (!prevAdList.get(i).equals(newAdList.get(i))) {
                return findNextAd(newAdList, -1);
            }
        }

        return findNextAd(newAdList, curAdIndex);
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

    private static List<MutableComponent> formatAds(List<String> ads) {
        return ads.stream().map(AutoAdvert::formatAd).toList();
    }

    private static MutableComponent formatAd(String input) {
        MutableComponent text = Component.literal("");
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
                    text = text.append(Component.literal(buffer.toString()).setStyle(currentStyle.applyFormats()));
                    buffer = new StringBuilder();
                }
                char nextChar = input.charAt(i + 1);
                nextStyle = nextStyle.withColor(ChatFormatting.getByCode(nextChar));
                i++;
            } else if (i + 13 < input.length() && RegexFilters.rgbFilter.matcher(input.substring(i, i + 14)).matches()) {
                if (!buffer.isEmpty()) {
                    text = text.append(Component.literal(buffer.toString()).setStyle(currentStyle.applyFormats()));
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
                text = text.append(Component.literal(buffer.toString()).setStyle(currentStyle.applyFormats()));
                buffer = new StringBuilder();
            }

            currentStyle = nextStyle;
        }

        if (!buffer.isEmpty()) {
            text = text.append(Component.literal(buffer.toString()).setStyle(currentStyle.applyFormats()));
        }

        if (input.startsWith("//")) {
            text = Component.literal(text.getString().substring(2)).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.STRIKETHROUGH);
        }

        return text;
    }

    private static String getAdFile() {
        String adFile;
        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock()) {
            return null;
        } else {
            switch (SbUtils.SERVER_DETECTOR.getCurrentServer()) {
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
}
