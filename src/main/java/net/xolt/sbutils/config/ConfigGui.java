package net.xolt.sbutils.config;

import dev.isxander.yacl.api.*;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl.gui.controllers.string.StringController;
import dev.isxander.yacl.gui.controllers.string.number.DoubleFieldController;
import dev.isxander.yacl.gui.controllers.string.number.IntegerFieldController;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.xolt.sbutils.util.LimitedList;

public class ConfigGui {

    public static Screen getModConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(ModConfig.INSTANCE, (defaults, config, builder) -> builder
                .title(Text.translatable("text.sbutils.config.title"))
                .category(buildSbutilsCategory(defaults, config))
                .category(buildAutoAdvertCategory(defaults, config))
                .category(buildJoinCommandsCategory(defaults, config))
                .category(buildMentionsCategory(defaults, config))
                .category(buildEnchantAllCategory(defaults, config))
                .category(buildChatAppendCategory(defaults, config))
                .category(buildChatFiltersCategory(defaults, config))
                .category(buildChatLoggerCategory(defaults, config))
                .category(buildAutoMineCategory(defaults, config))
                .category(buildAutoFixCategory(defaults, config))
                .category(buildToolSaverCategory(defaults, config))
                .category(buildAntiPlaceCategory(defaults, config))
                .category(buildAutoCommandCategory(defaults, config))
                .category(buildAutoReplyCategory(defaults, config))
                .category(buildAutoRaffleCategory(defaults, config))
                .category(buildAutoPrivateCategory(defaults, config))
                .category(buildAutoSilkCategory(defaults, config))
                .category(buildStaffDetectorCategory(defaults, config))
                .save(ModConfig.INSTANCE::save))
                .generateScreen(parent);
    }

    private static ConfigCategory buildSbutilsCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.default"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.sbutils"))
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.messagePrefix"))
                                .tooltip(Text.translatable("text.sbutils.config.option.messagePrefix.tooltip"))
                                .binding(
                                        defaults.messagePrefix,
                                        () -> config.messagePrefix,
                                        (value) -> config.messagePrefix = value
                                )
                                .controller(StringController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.Color.class)
                                .name(Text.translatable("text.sbutils.config.option.sbutilsColor"))
                                .tooltip(Text.translatable("text.sbutils.config.option.sbutilsColor.tooltip"))
                                .binding(
                                        defaults.sbutilsColor,
                                        () -> config.sbutilsColor,
                                        (value) -> config.sbutilsColor = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.Color.class)
                                .name(Text.translatable("text.sbutils.config.option.prefixColor"))
                                .tooltip(Text.translatable("text.sbutils.config.option.prefixColor.tooltip"))
                                .binding(
                                        defaults.prefixColor,
                                        () -> config.prefixColor,
                                        (value) -> config.prefixColor = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.Color.class)
                                .name(Text.translatable("text.sbutils.config.option.messageColor"))
                                .tooltip(Text.translatable("text.sbutils.config.option.messageColor.tooltip"))
                                .binding(
                                        defaults.messageColor,
                                        () -> config.messageColor,
                                        (value) -> config.messageColor = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.Color.class)
                                .name(Text.translatable("text.sbutils.config.option.valueColor"))
                                .tooltip(Text.translatable("text.sbutils.config.option.valueColor.tooltip"))
                                .binding(
                                        defaults.valueColor,
                                        () -> config.valueColor,
                                        (value) -> config.valueColor = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoAdvertCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoadvert"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoAdvert"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoAdvert.tooltip"))
                                .binding(
                                        defaults.autoAdvert,
                                        () -> config.autoAdvert,
                                        (value) -> config.autoAdvert = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.skyblockAdFile"))
                                .tooltip(Text.translatable("text.sbutils.config.option.skyblockAdFile.tooltip"))
                                .binding(
                                        defaults.skyblockAdFile,
                                        () -> config.skyblockAdFile,
                                        (value) -> config.skyblockAdFile = value
                                )
                                .controller(StringController::new)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.economyAdFile"))
                                .tooltip(Text.translatable("text.sbutils.config.option.economyAdFile.tooltip"))
                                .binding(
                                        defaults.economyAdFile,
                                        () -> config.economyAdFile,
                                        (value) -> config.economyAdFile = value
                                )
                                .controller(StringController::new)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.classicAdFile"))
                                .tooltip(Text.translatable("text.sbutils.config.option.classicAdFile.tooltip"))
                                .binding(
                                        defaults.classicAdFile,
                                        () -> config.classicAdFile,
                                        (value) -> config.classicAdFile = value
                                )
                                .controller(StringController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.advertDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.advertDelay.tooltip"))
                                .binding(
                                        defaults.advertDelay,
                                        () -> config.advertDelay,
                                        (value) -> config.advertDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.advertInitialDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.advertInitialDelay.tooltip"))
                                .binding(
                                        defaults.advertInitialDelay,
                                        () -> config.advertInitialDelay,
                                        (value) -> config.advertInitialDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.advertUseWhitelist"))
                                .tooltip(Text.translatable("text.sbutils.config.option.advertUseWhitelist.tooltip"))
                                .binding(
                                        defaults.advertUseWhitelist,
                                        () -> config.advertUseWhitelist,
                                        (value) -> config.advertUseWhitelist = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .group(ListOption.createBuilder(String.class)
                        .name(Text.translatable("text.sbutils.config.option.advertWhitelist"))
                        .tooltip(Text.translatable("text.sbutils.config.option.advertWhitelist.tooltip"))
                        .binding(
                                defaults.advertWhitelist,
                                () -> config.advertWhitelist,
                                (value) -> config.advertWhitelist = value
                        )
                        .controller(StringController::new)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildJoinCommandsCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.joincommands"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.joinCmds"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.joinCmdsEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.joinCmdsEnabled.tooltip"))
                                .binding(
                                        defaults.joinCmdsEnabled,
                                        () -> config.joinCmdsEnabled,
                                        (value) -> config.joinCmdsEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.joinCmdInitialDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.joinCmdInitialDelay.tooltip"))
                                .binding(
                                        defaults.joinCmdInitialDelay,
                                        () -> config.joinCmdInitialDelay,
                                        (value) -> config.joinCmdInitialDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.joinCmdDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.joinCmdDelay.tooltip"))
                                .binding(
                                        defaults.joinCmdDelay,
                                        () -> config.joinCmdDelay,
                                        (value) -> config.joinCmdDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .build())
                .group(ListOption.createBuilder(String.class)
                        .name(Text.translatable("text.sbutils.config.option.joinCmdServers"))
                        .tooltip(Text.translatable("text.sbutils.config.option.joinCmdServers.tooltip"))
                        .binding(
                                defaults.joinCmdServers,
                                () -> config.joinCmdServers,
                                (value) -> config.joinCmdServers = value
                        )
                        .controller(StringController::new)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildMentionsCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.mentions"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.mentions"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.mentions"))
                                .tooltip(Text.translatable("text.sbutils.config.option.mentions.tooltip"))
                                .binding(
                                        defaults.mentions,
                                        () -> config.mentions,
                                        (value) -> config.mentions = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.excludeServerMsgs"))
                                .tooltip(Text.translatable("text.sbutils.config.option.excludeServerMsgs.tooltip"))
                                .binding(
                                        defaults.excludeServerMsgs,
                                        () -> config.excludeServerMsgs,
                                        (value) -> config.excludeServerMsgs = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.excludeSelfMsgs"))
                                .tooltip(Text.translatable("text.sbutils.config.option.excludeSelfMsgs.tooltip"))
                                .binding(
                                        defaults.excludeSelfMsgs,
                                        () -> config.excludeSelfMsgs,
                                        (value) -> config.excludeSelfMsgs = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.NotifSound.class)
                                .name(Text.translatable("text.sbutils.config.option.mentionSound"))
                                .tooltip(Text.translatable("text.sbutils.config.option.mentionSound.tooltip"))
                                .binding(
                                        defaults.mentionSound,
                                        () -> config.mentionSound,
                                        (value) -> config.mentionSound = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.mentionHighlight"))
                                .tooltip(Text.translatable("text.sbutils.config.option.mentionHighlight.tooltip"))
                                .binding(
                                        defaults.mentionHighlight,
                                        () -> config.mentionHighlight,
                                        (value) -> config.mentionHighlight = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.Color.class)
                                .name(Text.translatable("text.sbutils.config.option.highlightColor"))
                                .tooltip(Text.translatable("text.sbutils.config.option.highlightColor.tooltip"))
                                .binding(
                                        defaults.highlightColor,
                                        () -> config.highlightColor,
                                        (value) -> config.highlightColor = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.mentionsCurrentAccount"))
                                .tooltip(Text.translatable("text.sbutils.config.option.mentionsCurrentAccount.tooltip"))
                                .binding(
                                        defaults.mentionsCurrentAccount,
                                        () -> config.mentionsCurrentAccount,
                                        (value) -> config.mentionsCurrentAccount = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .group(ListOption.createBuilder(String.class)
                        .name(Text.translatable("text.sbutils.config.option.mentionsAliases"))
                        .tooltip(Text.translatable("text.sbutils.config.option.mentionsAliases.tooltip"))
                        .binding(
                                defaults.mentionsAliases,
                                () -> config.mentionsAliases,
                                (value) -> config.mentionsAliases = value
                        )
                        .controller(StringController::new)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildEnchantAllCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.enchantall"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.enchantAll"))
                        .option(Option.createBuilder(ModConfig.EnchantMode.class)
                                .name(Text.translatable("text.sbutils.config.option.enchantMode"))
                                .tooltip(Text.translatable("text.sbutils.config.option.enchantMode.tooltip"))
                                .binding(
                                        defaults.enchantMode,
                                        () -> config.enchantMode,
                                        (value) -> config.enchantMode = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.enchantDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.enchantDelay.tooltip"))
                                .binding(
                                        defaults.enchantDelay,
                                        () -> config.enchantDelay,
                                        (value) -> config.enchantDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("text.sbutils.config.option.cooldownFrequency"))
                                .tooltip(Text.translatable("text.sbutils.config.option.cooldownFrequency.tooltip"))
                                .binding(
                                        defaults.cooldownFrequency,
                                        () -> config.cooldownFrequency,
                                        (value) -> config.cooldownFrequency = value
                                )
                                .controller(IntegerFieldController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.cooldownTime"))
                                .tooltip(Text.translatable("text.sbutils.config.option.cooldownTime.tooltip"))
                                .binding(
                                        defaults.cooldownTime,
                                        () -> config.cooldownTime,
                                        (value) -> config.cooldownTime = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.excludeFrost"))
                                .tooltip(Text.translatable("text.sbutils.config.option.excludeFrost.tooltip"))
                                .binding(
                                        defaults.excludeFrost,
                                        () -> config.excludeFrost,
                                        (value) -> config.excludeFrost = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatAppendCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatappend"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatAppend"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.addPrefix"))
                                .tooltip(Text.translatable("text.sbutils.config.option.addPrefix.tooltip"))
                                .binding(
                                        defaults.addPrefix,
                                        () -> config.addPrefix,
                                        (value) -> config.addPrefix = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.chatPrefix"))
                                .tooltip(Text.translatable("text.sbutils.config.option.chatPrefix.tooltip"))
                                .binding(
                                        defaults.chatPrefix,
                                        () -> config.chatPrefix,
                                        (value) -> config.chatPrefix = value
                                )
                                .controller(StringController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.addSuffix"))
                                .tooltip(Text.translatable("text.sbutils.config.option.addSuffix.tooltip"))
                                .binding(
                                        defaults.addSuffix,
                                        () -> config.addSuffix,
                                        (value) -> config.addSuffix = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.chatSuffix"))
                                .tooltip(Text.translatable("text.sbutils.config.option.chatSuffix.tooltip"))
                                .binding(
                                        defaults.chatSuffix,
                                        () -> config.chatSuffix,
                                        (value) -> config.chatSuffix = value
                                )
                                .controller(StringController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatFiltersCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatfilters"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatFilters"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.tipsFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.tipsFilterEnabled.tooltip"))
                                .binding(
                                        defaults.tipsFilterEnabled,
                                        () -> config.tipsFilterEnabled,
                                        (value) -> config.tipsFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.advancementsFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.advancementsFilterEnabled.tooltip"))
                                .binding(
                                        defaults.advancementsFilterEnabled,
                                        () -> config.advancementsFilterEnabled,
                                        (value) -> config.advancementsFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.welcomeFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.welcomeFilterEnabled.tooltip"))
                                .binding(
                                        defaults.welcomeFilterEnabled,
                                        () -> config.welcomeFilterEnabled,
                                        (value) -> config.welcomeFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.friendJoinFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.friendJoinFilterEnabled.tooltip"))
                                .binding(
                                        defaults.friendJoinFilterEnabled,
                                        () -> config.friendJoinFilterEnabled,
                                        (value) -> config.friendJoinFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.motdFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.motdFilterEnabled.tooltip"))
                                .binding(
                                        defaults.motdFilterEnabled,
                                        () -> config.motdFilterEnabled,
                                        (value) -> config.motdFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.voteFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.voteFilterEnabled.tooltip"))
                                .binding(
                                        defaults.voteFilterEnabled,
                                        () -> config.voteFilterEnabled,
                                        (value) -> config.voteFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.voteRewardFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.voteRewardFilterEnabled.tooltip"))
                                .binding(
                                        defaults.voteRewardFilterEnabled,
                                        () -> config.voteRewardFilterEnabled,
                                        (value) -> config.voteRewardFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.raffleFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.raffleFilterEnabled.tooltip"))
                                .binding(
                                        defaults.raffleFilterEnabled,
                                        () -> config.raffleFilterEnabled,
                                        (value) -> config.raffleFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.cratesFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.cratesFilterEnabled.tooltip"))
                                .binding(
                                        defaults.cratesFilterEnabled,
                                        () -> config.cratesFilterEnabled,
                                        (value) -> config.cratesFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.perishedInVoidFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.perishedInVoidFilterEnabled.tooltip"))
                                .binding(
                                        defaults.perishedInVoidFilterEnabled,
                                        () -> config.perishedInVoidFilterEnabled,
                                        (value) -> config.perishedInVoidFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.skyChatFilterEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.skyChatFilterEnabled.tooltip"))
                                .binding(
                                        defaults.skyChatFilterEnabled,
                                        () -> config.skyChatFilterEnabled,
                                        (value) -> config.skyChatFilterEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatLoggerCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatlogger"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatLogger"))
//                        .option(Option.createBuilder(boolean.class)
//                                .name(Text.translatable("text.sbutils.config.option.shopLoggerIncoming"))
//                                .tooltip(Text.translatable("text.sbutils.config.option.shopLoggerIncoming.tooltip"))
//                                .binding(
//                                        defaults.shopLoggerIncoming,
//                                        () -> config.shopLoggerIncoming,
//                                        (value) -> config.shopLoggerIncoming = value
//                                )
//                                .controller(TickBoxController::new)
//                                .build())
//                        .option(Option.createBuilder(boolean.class)
//                                .name(Text.translatable("text.sbutils.config.option.shopLoggerOutgoing"))
//                                .tooltip(Text.translatable("text.sbutils.config.option.shopLoggerOutgoing.tooltip"))
//                                .binding(
//                                        defaults.shopLoggerOutgoing,
//                                        () -> config.shopLoggerOutgoing,
//                                        (value) -> config.shopLoggerOutgoing = value
//                                )
//                                .controller(TickBoxController::new)
//                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.msgLoggerIncoming"))
                                .tooltip(Text.translatable("text.sbutils.config.option.msgLoggerIncoming.tooltip"))
                                .binding(
                                        defaults.msgLoggerIncoming,
                                        () -> config.msgLoggerIncoming,
                                        (value) -> config.msgLoggerIncoming = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.msgLoggerOutgoing"))
                                .tooltip(Text.translatable("text.sbutils.config.option.msgLoggerOutgoing.tooltip"))
                                .binding(
                                        defaults.msgLoggerOutgoing,
                                        () -> config.msgLoggerOutgoing,
                                        (value) -> config.msgLoggerOutgoing = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.visitLogger"))
                                .tooltip(Text.translatable("text.sbutils.config.option.visitLogger.tooltip"))
                                .binding(
                                        defaults.visitLogger,
                                        () -> config.visitLogger,
                                        (value) -> config.visitLogger = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.dpLogger"))
                                .tooltip(Text.translatable("text.sbutils.config.option.dpLogger.tooltip"))
                                .binding(
                                        defaults.dpLogger,
                                        () -> config.dpLogger,
                                        (value) -> config.dpLogger = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoMineCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.automine"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoMine"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoMine"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoMine.tooltip"))
                                .binding(
                                        defaults.autoMine,
                                        () -> config.autoMine,
                                        (value) -> config.autoMine = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoSwitch"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoSwitch.tooltip"))
                                .binding(
                                        defaults.autoSwitch,
                                        () -> config.autoSwitch,
                                        (value) -> config.autoSwitch = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("text.sbutils.config.option.switchDurability"))
                                .tooltip(Text.translatable("text.sbutils.config.option.switchDurability.tooltip"))
                                .binding(
                                        defaults.switchDurability,
                                        () -> config.switchDurability,
                                        (value) -> config.switchDurability = value
                                )
                                .controller(IntegerFieldController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoFixCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autofix"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoFix"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoFix"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoFix.tooltip"))
                                .binding(
                                        defaults.autoFix,
                                        () -> config.autoFix,
                                        (value) -> config.autoFix = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.FixMode.class)
                                .name(Text.translatable("text.sbutils.config.option.autoFixMode"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoFixMode.tooltip"))
                                .binding(
                                        defaults.autoFixMode,
                                        () -> config.autoFixMode,
                                        (value) -> config.autoFixMode = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.maxFixPercent"))
                                .tooltip(Text.translatable("text.sbutils.config.option.maxFixPercent.tooltip"))
                                .binding(
                                        defaults.maxFixPercent,
                                        () -> config.maxFixPercent,
                                        (value) -> config.maxFixPercent = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.autoFixDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoFixDelay.tooltip"))
                                .binding(
                                        defaults.autoFixDelay,
                                        () -> config.autoFixDelay,
                                        (value) -> config.autoFixDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.fixRetryDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.fixRetryDelay.tooltip"))
                                .binding(
                                        defaults.fixRetryDelay,
                                        () -> config.fixRetryDelay,
                                        (value) -> config.fixRetryDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("text.sbutils.config.option.maxFixRetries"))
                                .tooltip(Text.translatable("text.sbutils.config.option.maxFixRetries.tooltip"))
                                .binding(
                                        defaults.maxFixRetries,
                                        () -> config.maxFixRetries,
                                        (value) -> config.maxFixRetries = value
                                )
                                .controller(IntegerFieldController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildToolSaverCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.toolsaver"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.toolSaver"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.toolSaver"))
                                .tooltip(Text.translatable("text.sbutils.config.option.toolSaver.tooltip"))
                                .binding(
                                        defaults.toolSaver,
                                        () -> config.toolSaver,
                                        (value) -> config.toolSaver = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("text.sbutils.config.option.toolSaverDurability"))
                                .tooltip(Text.translatable("text.sbutils.config.option.toolSaverDurability.tooltip"))
                                .binding(
                                        defaults.toolSaverDurability,
                                        () -> config.toolSaverDurability,
                                        (value) -> config.toolSaverDurability = value
                                )
                                .controller(IntegerFieldController::new)
                                .build())
                        .build())
                .build();
    }

    public static ConfigCategory buildAntiPlaceCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.antiplace"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.antiPlace"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.antiPlaceHeads"))
                                .tooltip(Text.translatable("text.sbutils.config.option.antiPlaceHeads.tooltip"))
                                .binding(
                                        defaults.antiPlaceHeads,
                                        () -> config.antiPlaceHeads,
                                        (value) -> config.antiPlaceHeads = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.antiPlaceGrass"))
                                .tooltip(Text.translatable("text.sbutils.config.option.antiPlaceGrass.tooltip"))
                                .binding(
                                        defaults.antiPlaceGrass,
                                        () -> config.antiPlaceGrass,
                                        (value) -> config.antiPlaceGrass = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .build();
    }

    public static ConfigCategory buildAutoCommandCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autocommand"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoCommand"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoCommandEnabled"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoCommandEnabled.tooltip"))
                                .binding(
                                        defaults.autoCommandEnabled,
                                        () -> config.autoCommandEnabled,
                                        (value) -> config.autoCommandEnabled = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.autoCommand"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoCommand.tooltip"))
                                .binding(
                                        defaults.autoCommand,
                                        () -> config.autoCommand,
                                        (value) -> config.autoCommand = value
                                )
                                .controller(StringController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.autoCommandDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoCommandDelay.tooltip"))
                                .binding(
                                        defaults.autoCommandDelay,
                                        () -> config.autoCommandDelay,
                                        (value) -> config.autoCommandDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoReplyCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoreply"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoReply"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoReply"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoReply.tooltip"))
                                .binding(
                                        defaults.autoReply,
                                        () -> config.autoReply,
                                        (value) -> config.autoReply = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(String.class)
                                .name(Text.translatable("text.sbutils.config.option.autoResponse"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoResponse.tooltip"))
                                .binding(
                                        defaults.autoResponse,
                                        () -> config.autoResponse,
                                        (value) -> config.autoResponse = value
                                )
                                .controller(StringController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.autoReplyDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoReplyDelay.tooltip"))
                                .binding(
                                        defaults.autoReplyDelay,
                                        () -> config.autoReplyDelay,
                                        (value) -> config.autoReplyDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoRaffleCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoraffle"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoRaffle"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoRaffle"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoRaffle.tooltip"))
                                .binding(
                                        defaults.autoRaffle,
                                        () -> config.autoRaffle,
                                        (value) -> config.autoRaffle = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("text.sbutils.config.option.skyblockRaffleTickets"))
                                .tooltip(Text.translatable("text.sbutils.config.option.skyblockRaffleTickets.tooltip"))
                                .binding(
                                        defaults.skyblockRaffleTickets,
                                        () -> config.skyblockRaffleTickets,
                                        (value) -> config.skyblockRaffleTickets = value
                                )
                                .controller(integerOption -> new IntegerSliderController(integerOption, 1, 2, 1))
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("text.sbutils.config.option.economyRaffleTickets"))
                                .tooltip(Text.translatable("text.sbutils.config.option.economyRaffleTickets.tooltip"))
                                .binding(
                                        defaults.economyRaffleTickets,
                                        () -> config.economyRaffleTickets,
                                        (value) -> config.economyRaffleTickets = value
                                )
                                .controller(integerOption -> new IntegerSliderController(integerOption, 1, 5, 1))
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.grassCheckDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.grassCheckDelay.tooltip"))
                                .binding(
                                        defaults.grassCheckDelay,
                                        () -> config.grassCheckDelay,
                                        (value) -> config.grassCheckDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoPrivateCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoprivate"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoPrivate"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoPrivate"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoPrivate.tooltip"))
                                .binding(
                                        defaults.autoPrivate,
                                        () -> config.autoPrivate,
                                        (value) -> config.autoPrivate = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .group(ListOption.createBuilder(String.class)
                        .name(Text.translatable("text.sbutils.config.option.autoPrivateNames"))
                        .tooltip(Text.translatable("text.sbutils.config.option.autoPrivateNames.tooltip"))
                        .binding(
                                defaults.autoPrivateNames,
                                () -> config.autoPrivateNames,
                                (value) -> config.autoPrivateNames = new LimitedList<>(2, value)
                        )
                        .controller(StringController::new)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoSilkCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autosilk"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoSilk"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.autoSilk"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoSilk.tooltip"))
                                .binding(
                                        defaults.autoSilk,
                                        () -> config.autoSilk,
                                        (value) -> config.autoSilk = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.SilkTarget.class)
                                .name(Text.translatable("text.sbutils.config.option.targetTool"))
                                .tooltip(Text.translatable("text.sbutils.config.option.targetTool.tooltip"))
                                .binding(
                                        defaults.targetTool,
                                        () -> config.targetTool,
                                        (value) -> config.targetTool = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(double.class)
                                .name(Text.translatable("text.sbutils.config.option.autoSilkDelay"))
                                .tooltip(Text.translatable("text.sbutils.config.option.autoSilkDelay.tooltip"))
                                .binding(
                                        defaults.autoSilkDelay,
                                        () -> config.autoSilkDelay,
                                        (value) -> config.autoSilkDelay = value
                                )
                                .controller(DoubleFieldController::new)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildStaffDetectorCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.staffdetector"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.staffDetector"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.detectStaffJoin"))
                                .tooltip(Text.translatable("text.sbutils.config.option.detectStaffJoin.tooltip"))
                                .binding(
                                        defaults.detectStaffJoin,
                                        () -> config.detectStaffJoin,
                                        (value) -> config.detectStaffJoin = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.detectStaffLeave"))
                                .tooltip(Text.translatable("text.sbutils.config.option.detectStaffLeave.tooltip"))
                                .binding(
                                        defaults.detectStaffLeave,
                                        () -> config.detectStaffLeave,
                                        (value) -> config.detectStaffLeave = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("text.sbutils.config.option.playStaffSound"))
                                .tooltip(Text.translatable("text.sbutils.config.option.playStaffSound.tooltip"))
                                .binding(
                                        defaults.playStaffSound,
                                        () -> config.playStaffSound,
                                        (value) -> config.playStaffSound = value
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(ModConfig.NotifSound.class)
                                .name(Text.translatable("text.sbutils.config.option.staffDetectSound"))
                                .tooltip(Text.translatable("text.sbutils.config.option.staffDetectSound.tooltip"))
                                .binding(
                                        defaults.staffDetectSound,
                                        () -> config.staffDetectSound,
                                        (value) -> config.staffDetectSound = value
                                )
                                .controller(EnumController::new)
                                .build())
                        .build())
                .build();
    }
}
