package net.xolt.sbutils.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
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
                .category(buildNoGMTCategory(defaults, config))
                .category(buildEnchantAllCategory(defaults, config))
                .category(buildChatAppendCategory(defaults, config))
                .category(buildChatFiltersCategory(defaults, config))
                .category(buildChatLoggerCategory(defaults, config))
                .category(buildEventNotifierCategory(defaults, config))
                .category(buildAutoMineCategory(defaults, config))
                .category(buildAutoFixCategory(defaults, config))
                .category(buildToolSaverCategory(defaults, config))
                .category(buildAntiPlaceCategory(defaults, config))
                .category(buildAutoCommandCategory(defaults, config))
                .category(buildAutoReplyCategory(defaults, config))
                .category(buildAutoRaffleCategory(defaults, config))
                .category(buildAutoPrivateCategory(defaults, config))
                .category(buildAutoSilkCategory(defaults, config))
                .category(buildAutoCrateCategory(defaults, config))
                .category(buildStaffDetectorCategory(defaults, config))
                .save(ModConfig.INSTANCE::save))
                .generateScreen(parent);
    }

    private static ConfigCategory buildSbutilsCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.default"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.sbutils"))
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.messagePrefix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.messagePrefix.tooltip")))
                                .binding(
                                        defaults.messagePrefix,
                                        () -> config.messagePrefix,
                                        (value) -> config.messagePrefix = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.sbutilsColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.sbutilsColor.tooltip")))
                                .binding(
                                        defaults.sbutilsColor,
                                        () -> config.sbutilsColor,
                                        (value) -> config.sbutilsColor = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.Color.class))
                                .build())
                        .option(Option.<ModConfig.Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.prefixColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.prefixColor.tooltip")))
                                .binding(
                                        defaults.prefixColor,
                                        () -> config.prefixColor,
                                        (value) -> config.prefixColor = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.Color.class))
                                .build())
                        .option(Option.<ModConfig.Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.messageColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.messageColor.tooltip")))
                                .binding(
                                        defaults.messageColor,
                                        () -> config.messageColor,
                                        (value) -> config.messageColor = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.Color.class))
                                .build())
                        .option(Option.<ModConfig.Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.valueColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.valueColor.tooltip")))
                                .binding(
                                        defaults.valueColor,
                                        () -> config.valueColor,
                                        (value) -> config.valueColor = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.Color.class))
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoAdvertCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoadvert"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoAdvert"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.tooltip")))
                                .binding(
                                        defaults.autoAdvert,
                                        () -> config.autoAdvert,
                                        (value) -> config.autoAdvert = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.skyblockAdFile"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.skyblockAdFile.tooltip")))
                                .binding(
                                        defaults.skyblockAdFile,
                                        () -> config.skyblockAdFile,
                                        (value) -> config.skyblockAdFile = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.economyAdFile"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.economyAdFile.tooltip")))
                                .binding(
                                        defaults.economyAdFile,
                                        () -> config.economyAdFile,
                                        (value) -> config.economyAdFile = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.classicAdFile"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.classicAdFile.tooltip")))
                                .binding(
                                        defaults.classicAdFile,
                                        () -> config.classicAdFile,
                                        (value) -> config.classicAdFile = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.advertDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.advertDelay.tooltip")))
                                .binding(
                                        defaults.advertDelay,
                                        () -> config.advertDelay,
                                        (value) -> config.advertDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.advertInitialDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.advertInitialDelay.tooltip")))
                                .binding(
                                        defaults.advertInitialDelay,
                                        () -> config.advertInitialDelay,
                                        (value) -> config.advertInitialDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.advertUseWhitelist"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.advertUseWhitelist.tooltip")))
                                .binding(
                                        defaults.advertUseWhitelist,
                                        () -> config.advertUseWhitelist,
                                        (value) -> config.advertUseWhitelist = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.advertWhitelist"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.advertWhitelist.tooltip")))
                        .binding(
                                defaults.advertWhitelist,
                                () -> config.advertWhitelist,
                                (value) -> config.advertWhitelist = value
                        )
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildJoinCommandsCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.joincommands"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.joinCmds"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.joinCmdsEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.joinCmdsEnabled.tooltip")))
                                .binding(
                                        defaults.joinCmdsEnabled,
                                        () -> config.joinCmdsEnabled,
                                        (value) -> config.joinCmdsEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.joinCmdInitialDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.joinCmdInitialDelay.tooltip")))
                                .binding(
                                        defaults.joinCmdInitialDelay,
                                        () -> config.joinCmdInitialDelay,
                                        (value) -> config.joinCmdInitialDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.joinCmdDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.joinCmdDelay.tooltip")))
                                .binding(
                                        defaults.joinCmdDelay,
                                        () -> config.joinCmdDelay,
                                        (value) -> config.joinCmdDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildMentionsCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.mentions"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.mentions"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.tooltip")))
                                .binding(
                                        defaults.mentions,
                                        () -> config.mentions,
                                        (value) -> config.mentions = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.playMentionSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.playMentionSound.tooltip")))
                                .binding(
                                        defaults.playMentionSound,
                                        () -> config.playMentionSound,
                                        (value) -> config.playMentionSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentionSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentionSound.tooltip")))
                                .binding(
                                        defaults.mentionSound,
                                        () -> config.mentionSound,
                                        (value) -> config.mentionSound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentionHighlight"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentionHighlight.tooltip")))
                                .binding(
                                        defaults.mentionHighlight,
                                        () -> config.mentionHighlight,
                                        (value) -> config.mentionHighlight = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.highlightColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.highlightColor.tooltip")))
                                .binding(
                                        defaults.highlightColor,
                                        () -> config.highlightColor,
                                        (value) -> config.highlightColor = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.Color.class))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.excludeServerMsgs"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.excludeServerMsgs.tooltip")))
                                .binding(
                                        defaults.excludeServerMsgs,
                                        () -> config.excludeServerMsgs,
                                        (value) -> config.excludeServerMsgs = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.excludeSelfMsgs"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.excludeSelfMsgs.tooltip")))
                                .binding(
                                        defaults.excludeSelfMsgs,
                                        () -> config.excludeSelfMsgs,
                                        (value) -> config.excludeSelfMsgs = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.excludeSender"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.excludeSender.tooltip")))
                                .binding(
                                        defaults.excludeSender,
                                        () -> config.excludeSender,
                                        (value) -> config.excludeSender = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentionsCurrentAccount"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentionsCurrentAccount.tooltip")))
                                .binding(
                                        defaults.mentionsCurrentAccount,
                                        () -> config.mentionsCurrentAccount,
                                        (value) -> config.mentionsCurrentAccount = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.mentionsAliases"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentionsAliases.tooltip")))
                        .binding(
                                defaults.mentionsAliases,
                                () -> config.mentionsAliases,
                                (value) -> config.mentionsAliases = value
                        )
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildNoGMTCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.nogmt"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.noGMT"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.noGMT"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.noGMT.tooltip")))
                                .binding(
                                        defaults.noGMT,
                                        () -> config.noGMT,
                                        (value) -> config.noGMT = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.timeZone"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.timeZone.tooltip")))
                                .binding(
                                        defaults.timeZone,
                                        () -> config.timeZone,
                                        (value) -> config.timeZone = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.showTimeZone"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.showTimeZone.tooltip")))
                                .binding(
                                        defaults.showTimeZone,
                                        () -> config.showTimeZone,
                                        (value) -> config.showTimeZone = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildEnchantAllCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.enchantall"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.enchantAll"))
                        .option(Option.<ModConfig.EnchantMode>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.enchantMode"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.enchantMode.tooltip")))
                                .binding(
                                        defaults.enchantMode,
                                        () -> config.enchantMode,
                                        (value) -> config.enchantMode = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.EnchantMode.class))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.enchantDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.enchantDelay.tooltip")))
                                .binding(
                                        defaults.enchantDelay,
                                        () -> config.enchantDelay,
                                        (value) -> config.enchantDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.cooldownFrequency"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.cooldownFrequency.tooltip")))
                                .binding(
                                        defaults.cooldownFrequency,
                                        () -> config.cooldownFrequency,
                                        (value) -> config.cooldownFrequency = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.cooldownTime"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.cooldownTime.tooltip")))
                                .binding(
                                        defaults.cooldownTime,
                                        () -> config.cooldownTime,
                                        (value) -> config.cooldownTime = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.excludeFrost"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.excludeFrost.tooltip")))
                                .binding(
                                        defaults.excludeFrost,
                                        () -> config.excludeFrost,
                                        (value) -> config.excludeFrost = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatAppendCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatappend"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatAppend"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.addPrefix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.addPrefix.tooltip")))
                                .binding(
                                        defaults.addPrefix,
                                        () -> config.addPrefix,
                                        (value) -> config.addPrefix = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatPrefix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatPrefix.tooltip")))
                                .binding(
                                        defaults.chatPrefix,
                                        () -> config.chatPrefix,
                                        (value) -> config.chatPrefix = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.addSuffix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.addSuffix.tooltip")))
                                .binding(
                                        defaults.addSuffix,
                                        () -> config.addSuffix,
                                        (value) -> config.addSuffix = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatSuffix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatSuffix.tooltip")))
                                .binding(
                                        defaults.chatSuffix,
                                        () -> config.chatSuffix,
                                        (value) -> config.chatSuffix = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatFiltersCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatfilters"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatFilters"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.tipsFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.tipsFilterEnabled.tooltip")))
                                .binding(
                                        defaults.tipsFilterEnabled,
                                        () -> config.tipsFilterEnabled,
                                        (value) -> config.tipsFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.advancementsFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.advancementsFilterEnabled.tooltip")))
                                .binding(
                                        defaults.advancementsFilterEnabled,
                                        () -> config.advancementsFilterEnabled,
                                        (value) -> config.advancementsFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.welcomeFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.welcomeFilterEnabled.tooltip")))
                                .binding(
                                        defaults.welcomeFilterEnabled,
                                        () -> config.welcomeFilterEnabled,
                                        (value) -> config.welcomeFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.friendJoinFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.friendJoinFilterEnabled.tooltip")))
                                .binding(
                                        defaults.friendJoinFilterEnabled,
                                        () -> config.friendJoinFilterEnabled,
                                        (value) -> config.friendJoinFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.motdFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.motdFilterEnabled.tooltip")))
                                .binding(
                                        defaults.motdFilterEnabled,
                                        () -> config.motdFilterEnabled,
                                        (value) -> config.motdFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.voteFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.voteFilterEnabled.tooltip")))
                                .binding(
                                        defaults.voteFilterEnabled,
                                        () -> config.voteFilterEnabled,
                                        (value) -> config.voteFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.voteRewardFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.voteRewardFilterEnabled.tooltip")))
                                .binding(
                                        defaults.voteRewardFilterEnabled,
                                        () -> config.voteRewardFilterEnabled,
                                        (value) -> config.voteRewardFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.raffleFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.raffleFilterEnabled.tooltip")))
                                .binding(
                                        defaults.raffleFilterEnabled,
                                        () -> config.raffleFilterEnabled,
                                        (value) -> config.raffleFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.cratesFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.cratesFilterEnabled.tooltip")))
                                .binding(
                                        defaults.cratesFilterEnabled,
                                        () -> config.cratesFilterEnabled,
                                        (value) -> config.cratesFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.perishedInVoidFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.perishedInVoidFilterEnabled.tooltip")))
                                .binding(
                                        defaults.perishedInVoidFilterEnabled,
                                        () -> config.perishedInVoidFilterEnabled,
                                        (value) -> config.perishedInVoidFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.skyChatFilterEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.skyChatFilterEnabled.tooltip")))
                                .binding(
                                        defaults.skyChatFilterEnabled,
                                        () -> config.skyChatFilterEnabled,
                                        (value) -> config.skyChatFilterEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatLoggerCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatlogger"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatLogger"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.shopLoggerIncoming"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.shopLoggerIncoming.tooltip")))
                                .binding(
                                        defaults.shopLoggerIncoming,
                                        () -> config.shopLoggerIncoming,
                                        (value) -> config.shopLoggerIncoming = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.shopLoggerOutgoing"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.shopLoggerOutgoing.tooltip")))
                                .binding(
                                        defaults.shopLoggerOutgoing,
                                        () -> config.shopLoggerOutgoing,
                                        (value) -> config.shopLoggerOutgoing = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.msgLoggerIncoming"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.msgLoggerIncoming.tooltip")))
                                .binding(
                                        defaults.msgLoggerIncoming,
                                        () -> config.msgLoggerIncoming,
                                        (value) -> config.msgLoggerIncoming = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.msgLoggerOutgoing"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.msgLoggerOutgoing.tooltip")))
                                .binding(
                                        defaults.msgLoggerOutgoing,
                                        () -> config.msgLoggerOutgoing,
                                        (value) -> config.msgLoggerOutgoing = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.visitLogger"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.visitLogger.tooltip")))
                                .binding(
                                        defaults.visitLogger,
                                        () -> config.visitLogger,
                                        (value) -> config.visitLogger = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.dpLogger"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.dpLogger.tooltip")))
                                .binding(
                                        defaults.dpLogger,
                                        () -> config.dpLogger,
                                        (value) -> config.dpLogger = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildEventNotifierCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.eventnotifier"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.eventNotifier"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.showLlamaTitle"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.showLlamaTitle.tooltip")))
                                .binding(
                                        defaults.showLlamaTitle,
                                        () -> config.showLlamaTitle,
                                        (value) -> config.showLlamaTitle = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.playLlamaSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.playLlamaSound.tooltip")))
                                .binding(
                                        defaults.playLlamaSound,
                                        () -> config.playLlamaSound,
                                        (value) -> config.playLlamaSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.llamaSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.llamaSound.tooltip")))
                                .binding(
                                        defaults.llamaSound,
                                        () -> config.llamaSound,
                                        (value) -> config.llamaSound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.showTraderTitle"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.showTraderTitle.tooltip")))
                                .binding(
                                        defaults.showTraderTitle,
                                        () -> config.showTraderTitle,
                                        (value) -> config.showTraderTitle = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.playTraderSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.playTraderSound.tooltip")))
                                .binding(
                                        defaults.playTraderSound,
                                        () -> config.playTraderSound,
                                        (value) -> config.playTraderSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.traderSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.traderSound.tooltip")))
                                .binding(
                                        defaults.traderSound,
                                        () -> config.traderSound,
                                        (value) -> config.traderSound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoMineCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.automine"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoMine"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoMine"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoMine.tooltip")))
                                .binding(
                                        defaults.autoMine,
                                        () -> config.autoMine,
                                        (value) -> config.autoMine = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSwitch"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSwitch.tooltip")))
                                .binding(
                                        defaults.autoSwitch,
                                        () -> config.autoSwitch,
                                        (value) -> config.autoSwitch = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.switchDurability"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.switchDurability.tooltip")))
                                .binding(
                                        defaults.switchDurability,
                                        () -> config.switchDurability,
                                        (value) -> config.switchDurability = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoFixCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autofix"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoFix"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFix.tooltip")))
                                .binding(
                                        defaults.autoFix,
                                        () -> config.autoFix,
                                        (value) -> config.autoFix = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.FixMode>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFixMode"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFixMode.tooltip")))
                                .binding(
                                        defaults.autoFixMode,
                                        () -> config.autoFixMode,
                                        (value) -> config.autoFixMode = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.FixMode.class))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.maxFixPercent"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.maxFixPercent.tooltip")))
                                .binding(
                                        defaults.maxFixPercent,
                                        () -> config.maxFixPercent,
                                        (value) -> config.maxFixPercent = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFixDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFixDelay.tooltip")))
                                .binding(
                                        defaults.autoFixDelay,
                                        () -> config.autoFixDelay,
                                        (value) -> config.autoFixDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.fixRetryDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.fixRetryDelay.tooltip")))
                                .binding(
                                        defaults.fixRetryDelay,
                                        () -> config.fixRetryDelay,
                                        (value) -> config.fixRetryDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.maxFixRetries"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.maxFixRetries.tooltip")))
                                .binding(
                                        defaults.maxFixRetries,
                                        () -> config.maxFixRetries,
                                        (value) -> config.maxFixRetries = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildToolSaverCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.toolsaver"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.toolSaver"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.toolSaver"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.toolSaver.tooltip")))
                                .binding(
                                        defaults.toolSaver,
                                        () -> config.toolSaver,
                                        (value) -> config.toolSaver = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.toolSaverDurability"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.toolSaverDurability.tooltip")))
                                .binding(
                                        defaults.toolSaverDurability,
                                        () -> config.toolSaverDurability,
                                        (value) -> config.toolSaverDurability = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    public static ConfigCategory buildAntiPlaceCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.antiplace"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.antiPlace"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.antiPlaceHeads"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.antiPlaceHeads.tooltip")))
                                .binding(
                                        defaults.antiPlaceHeads,
                                        () -> config.antiPlaceHeads,
                                        (value) -> config.antiPlaceHeads = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.antiPlaceGrass"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.antiPlaceGrass.tooltip")))
                                .binding(
                                        defaults.antiPlaceGrass,
                                        () -> config.antiPlaceGrass,
                                        (value) -> config.antiPlaceGrass = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    public static ConfigCategory buildAutoCommandCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autocommand"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoCommand"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCommandEnabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCommandEnabled.tooltip")))
                                .binding(
                                        defaults.autoCommandEnabled,
                                        () -> config.autoCommandEnabled,
                                        (value) -> config.autoCommandEnabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCommand"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCommand.tooltip")))
                                .binding(
                                        defaults.autoCommand,
                                        () -> config.autoCommand,
                                        (value) -> config.autoCommand = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCommandDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCommandDelay.tooltip")))
                                .binding(
                                        defaults.autoCommandDelay,
                                        () -> config.autoCommandDelay,
                                        (value) -> config.autoCommandDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoReplyCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoreply"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoReply"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoReply"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoReply.tooltip")))
                                .binding(
                                        defaults.autoReply,
                                        () -> config.autoReply,
                                        (value) -> config.autoReply = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoResponse"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoResponse.tooltip")))
                                .binding(
                                        defaults.autoResponse,
                                        () -> config.autoResponse,
                                        (value) -> config.autoResponse = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoReplyDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoReplyDelay.tooltip")))
                                .binding(
                                        defaults.autoReplyDelay,
                                        () -> config.autoReplyDelay,
                                        (value) -> config.autoReplyDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoRaffleCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoraffle"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoRaffle"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoRaffle"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoRaffle.tooltip")))
                                .binding(
                                        defaults.autoRaffle,
                                        () -> config.autoRaffle,
                                        (value) -> config.autoRaffle = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.skyblockRaffleTickets"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.skyblockRaffleTickets.tooltip")))
                                .binding(
                                        defaults.skyblockRaffleTickets,
                                        () -> config.skyblockRaffleTickets,
                                        (value) -> config.skyblockRaffleTickets = value
                                )
                                .customController(integerOption -> new IntegerSliderController(integerOption, 1, 2, 1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.economyRaffleTickets"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.economyRaffleTickets.tooltip")))
                                .binding(
                                        defaults.economyRaffleTickets,
                                        () -> config.economyRaffleTickets,
                                        (value) -> config.economyRaffleTickets = value
                                )
                                .customController(integerOption -> new IntegerSliderController(integerOption, 1, 2, 1))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.grassCheckDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.grassCheckDelay.tooltip")))
                                .binding(
                                        defaults.grassCheckDelay,
                                        () -> config.grassCheckDelay,
                                        (value) -> config.grassCheckDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoPrivateCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoprivate"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoPrivate"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoPrivate"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoPrivate.tooltip")))
                                .binding(
                                        defaults.autoPrivate,
                                        () -> config.autoPrivate,
                                        (value) -> config.autoPrivate = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.autoPrivateNames"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoPrivateNames.tooltip")))
                        .maximumNumberOfEntries(2)
                        .insertEntriesAtEnd(true)
                        .binding(
                                defaults.autoPrivateNames,
                                () -> config.autoPrivateNames,
                                (value) -> config.autoPrivateNames = new LimitedList<>(2, value)
                        )
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoSilkCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autosilk"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoSilk"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilk"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilk.tooltip")))
                                .binding(
                                        defaults.autoSilk,
                                        () -> config.autoSilk,
                                        (value) -> config.autoSilk = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.SilkTarget>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.targetTool"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.targetTool.tooltip")))
                                .binding(
                                        defaults.targetTool,
                                        () -> config.targetTool,
                                        (value) -> config.targetTool = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.SilkTarget.class))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilkDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilkDelay.tooltip")))
                                .binding(
                                        defaults.autoSilkDelay,
                                        () -> config.autoSilkDelay,
                                        (value) -> config.autoSilkDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoCrateCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autocrate"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoCrate"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCrate"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCrate.tooltip")))
                                .binding(
                                        defaults.autoCrate,
                                        () -> config.autoCrate,
                                        (value) -> config.autoCrate = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.CrateMode>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.crateMode"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.crateMode.tooltip")))
                                .binding(
                                        defaults.crateMode,
                                        () -> config.crateMode,
                                        (value) -> config.crateMode = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.CrateMode.class))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.crateDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.crateDelay.tooltip")))
                                .binding(
                                        defaults.crateDelay,
                                        () -> config.crateDelay,
                                        (value) -> config.crateDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.crateDistance"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.crateDistance.tooltip")))
                                .binding(
                                        defaults.crateDistance,
                                        () -> config.crateDistance,
                                        (value) -> config.crateDistance = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildStaffDetectorCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.staffdetector"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.staffDetector"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.detectStaffJoin"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.detectStaffJoin.tooltip")))
                                .binding(
                                        defaults.detectStaffJoin,
                                        () -> config.detectStaffJoin,
                                        (value) -> config.detectStaffJoin = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.detectStaffLeave"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.detectStaffLeave.tooltip")))
                                .binding(
                                        defaults.detectStaffLeave,
                                        () -> config.detectStaffLeave,
                                        (value) -> config.detectStaffLeave = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.playStaffSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.playStaffSound.tooltip")))
                                .binding(
                                        defaults.playStaffSound,
                                        () -> config.playStaffSound,
                                        (value) -> config.playStaffSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.staffDetectSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.staffDetectSound.tooltip")))
                                .binding(
                                        defaults.staffDetectSound,
                                        () -> config.staffDetectSound,
                                        (value) -> config.staffDetectSound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .build())
                .build();
    }
}
