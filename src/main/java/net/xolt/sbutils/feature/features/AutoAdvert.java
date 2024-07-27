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
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;
import static net.xolt.sbutils.SbUtils.SERVER_DETECTOR;

public class AutoAdvert extends Feature {

    private final OptionBinding<Boolean> enabled = new OptionBinding<>("autoAdvert.enabled", Boolean.class, (config) -> config.autoAdvert.enabled, (config, value) -> config.autoAdvert.enabled = value);
    private final OptionBinding<String> sbFile = new OptionBinding<>("autoAdvert.sbFile", String.class, (config) -> config.autoAdvert.sbFile, (config, value) -> config.autoAdvert.sbFile = value);
    private final OptionBinding<Double> sbDelay = new OptionBinding<>("autoAdvert.sbDelay", Double.class, (config) -> config.autoAdvert.sbDelay, (config, value) -> config.autoAdvert.sbDelay = value);
    private final OptionBinding<String> ecoFile = new OptionBinding<>("autoAdvert.ecoFile", String.class, (config) -> config.autoAdvert.ecoFile, (config, value) -> config.autoAdvert.ecoFile = value);
    private final OptionBinding<Double> ecoDelay = new OptionBinding<>("autoAdvert.ecoDelay", Double.class, (config) -> config.autoAdvert.ecoDelay, (config, value) -> config.autoAdvert.ecoDelay = value);
    private final OptionBinding<String> classicFile = new OptionBinding<>("autoAdvert.classicFile", String.class, (config) -> config.autoAdvert.classicFile, (config, value) -> config.autoAdvert.classicFile = value);
    private final OptionBinding<Double> classicDelay = new OptionBinding<>("autoAdvert.classicDelay", Double.class, (config) -> config.autoAdvert.classicDelay, (config, value) -> config.autoAdvert.classicDelay = value);
    private final OptionBinding<Double> initialDelay = new OptionBinding<>("autoAdvert.initialDelay", Double.class, (config) -> config.autoAdvert.initialDelay, (config, value) -> config.autoAdvert.initialDelay = value);
    private final OptionBinding<Boolean> useWhitelist = new OptionBinding<>("autoAdvert.useWhitelist", Boolean.class, (config) -> config.autoAdvert.useWhitelist, (config, value) -> config.autoAdvert.useWhitelist = value);
    private final ListOptionBinding<String> whitelist = new ListOptionBinding<>("autoAdvert.whitelist", "", String.class, (config) -> config.autoAdvert.whitelist, (config, value) -> config.autoAdvert.whitelist = value, new ListConstraints<>(false, null, new StringConstraints(false)));

    private List<String> prevAdList;
    private int adIndex;
    private long lastAdSentAt;
    private long joinedAt;

    public AutoAdvert() {
        super("autoAdvert", "autoadvert", "advert");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, sbFile, sbDelay, ecoFile, ecoDelay, classicFile, classicDelay, initialDelay, useWhitelist, whitelist);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoAdvertNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                    .then(CommandHelper.runnable("info", this::onInfoCommand))
                    .then(CommandHelper.string("sbFile", "filename", sbFile))
                    .then(CommandHelper.string("ecoFile", "filename", ecoFile))
                    .then(CommandHelper.string("classicFile", "filename", classicFile))
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
                    .then(CommandHelper.doubl("sbDelay", "seconds", sbDelay))
                    .then(CommandHelper.doubl("ecoDelay", "seconds", ecoDelay))
                    .then(CommandHelper.doubl("classicDelay", "seconds", classicDelay))
                    .then(CommandHelper.doubl("initialDelay", "seconds", initialDelay))
                    .then(CommandHelper.stringList("whitelist", "user", whitelist)
                            .then(CommandHelper.bool("enabled", useWhitelist)))
                    .then(CommandHelper.runnable("reset", () -> {
                        reset();
                        ChatUtils.printMessage("message.sbutils.autoAdvert.reset");
                    }))
        );
        registerAlias(dispatcher, autoAdvertNode);
    }

    private void onInfoCommand() {
        if (!ModConfig.HANDLER.instance().autoAdvert.enabled) {
            ChatUtils.printSetting("text.sbutils.config.category.autoAdvert", false);
            return;
        }

        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock()) {
            ChatUtils.printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }

        if (ModConfig.HANDLER.instance().autoAdvert.useWhitelist && !userWhitelisted()) {
            ChatUtils.printMessage("message.sbutils.autoAdvert.notWhitelisted");
            return;
        }

        int remainingDelay = delayLeft();
        int delayMinutes = remainingDelay / 60000;
        double delaySeconds = Math.round((double)(remainingDelay % 60000) / 100.0) / 10.0;

        MutableComponent message;
        MutableComponent index = Component.literal("#" + (adIndex + 1));
        MutableComponent seconds = Component.literal(String.valueOf(delaySeconds));

        if (delayMinutes > 0) {
            message = Component.translatable("message.sbutils.autoAdvert.infoWithMinutes");
            MutableComponent minutes = Component.literal(String.valueOf(delayMinutes));
            ChatUtils.printWithPlaceholders(message, index, minutes, seconds);
        } else {
            message = Component.translatable("message.sbutils.autoAdvert.infoJustSeconds");
            ChatUtils.printWithPlaceholders(message, index, seconds);
        }
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

        ChatUtils.printMessage("message.sbutils.autoAdvert.addSuccess");
        ChatUtils.printList(formatAds(adverts), true);
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

        ChatUtils.printMessage("message.sbutils.autoAdvert.deleteSuccess");
        ChatUtils.printList(formatAds(adverts), true);
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

        ChatUtils.printMessage("message.sbutils.autoAdvert.addSuccess");
        ChatUtils.printList(formatAds(adverts), true);
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
        if (currentValue.startsWith("//"))
            adverts.set(adjustedIndex, currentValue.substring(2));
        else
            adverts.set(adjustedIndex, "//" + currentValue);

        IOHandler.writeAdverts(adverts, adFile);

        ChatUtils.printMessage("message.sbutils.autoAdvert.toggleSuccess");
        ChatUtils.printList(formatAds(adverts), true);
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoAdvert.enabled || MC.getConnection() == null)
            return;

        if (MC.screen instanceof ReceivingLevelScreen)
            joinedAt = System.currentTimeMillis();

        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock() || (ModConfig.HANDLER.instance().autoAdvert.useWhitelist && !userWhitelisted()))
            return;

        if (delayLeft() > 0)
            return;

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
        if (!ModConfig.HANDLER.instance().autoAdvert.enabled)
            return;

        joinedAt = System.currentTimeMillis();
    }

    public void onSwitchServer() {
        refreshPrevAdlist();
    }

    private void sendAd() {
        if (MC.getConnection() == null)
            return;

        MC.getConnection().sendChat(prevAdList.get(adIndex));
    }

    private int delayLeft() {
        long delay = (long)(delay() * 1000.0);
        long initialDelay = (long)(ModConfig.HANDLER.instance().autoAdvert.initialDelay * 1000.0);

        int delayLeft = (int)Math.max(delay - (System.currentTimeMillis() - lastAdSentAt), 0L);
        int initialDelayLeft = (int)Math.max(initialDelay - (System.currentTimeMillis() - joinedAt), 0L);

        return Math.max(delayLeft, initialDelayLeft);
    }

    private double delay() {
        return switch(SERVER_DETECTOR.getCurrentServer()) {
            case ECONOMY -> ecoDelay.get(ModConfig.HANDLER.instance());
            case CLASSIC -> classicDelay.get(ModConfig.HANDLER.instance());
            default -> sbDelay.get(ModConfig.HANDLER.instance());
        };
    }

    private void refreshPrevAdlist() {
        prevAdList = getAdList();
    }

    private void reset() {
        lastAdSentAt = 0;
        adIndex = 0;
    }

    private static int getUpdatedAdIndex(List<String> newAdList, List<String> prevAdList, int curAdIndex) {
        if (newAdList == null || prevAdList == null || prevAdList.size() != newAdList.size())
            return findNextAd(newAdList, -1);

        for (int i = 0; i < prevAdList.size(); i++)
            if (!prevAdList.get(i).equals(newAdList.get(i)))
                return findNextAd(newAdList, -1);

        return findNextAd(newAdList, curAdIndex);
    }

    private static int findNextAd(List<String> newAdList, int index) {
        if (newAdList == null)
            return -1;

        int startIndex = index + 1;
        for (int i = 0; i < newAdList.size(); i++) {
            int currentIndex = (startIndex + i) % newAdList.size();
            if (!newAdList.get(currentIndex).startsWith("//"))
                return currentIndex;
        }

        return -1;
    }

    private static List<String> getAdList() {
        String adFile = getAdFile();
        if (adFile == null)
            return new ArrayList<>();

        String adListString = IOHandler.readAdFile(getAdFile());

        if (adListString == null || adListString.isEmpty())
            return new ArrayList<>();

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

        if (!buffer.isEmpty())
            text = text.append(Component.literal(buffer.toString()).setStyle(currentStyle.applyFormats()));

        if (input.startsWith("//"))
            text = Component.literal(text.getString().substring(2)).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.STRIKETHROUGH);

        return text;
    }

    private static String getAdFile() {
        String adFile;
        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock())
            return null;
        else
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

        if (!adFile.endsWith(".txt")) {
            return adFile + ".txt";
        }

        return adFile;
    }

    private static boolean userWhitelisted() {
        if (MC.player == null)
            return false;

        return getWhitelist().contains(MC.player.getGameProfile().getName());
    }

    private static List<String> getWhitelist() {
        return ModConfig.HANDLER.instance().autoAdvert.whitelist;
    }
}
