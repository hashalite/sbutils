package net.xolt.sbutils.config.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.gui.controllers.AutoCommandEntryController;
import net.xolt.sbutils.features.AutoFix;
import net.xolt.sbutils.features.AutoKit;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class ConfigGui {

    public static Screen getConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(ModConfig.HANDLER, (defaults, config, builder) -> builder
                        .title(Text.translatable("text.sbutils.config.title"))
                        .category(buildSbutilsCategory(defaults, config))
                        .category(buildAntiPlaceCategory(defaults, config))
                        .category(buildAutoAdvertCategory(defaults, config))
                        .category(buildAutoCommandCategory(defaults, config))
                        .category(buildAutoCrateCategory(defaults, config))
                        .category(buildAutoFixCategory(defaults, config))
                        .category(buildAutoKitCategory(defaults, config))
                        .category(buildAutoMineCategory(defaults, config))
                        .category(buildAutoPrivateCategory(defaults, config))
                        .category(buildAutoRaffleCategory(defaults, config))
                        .category(buildAutoReplyCategory(defaults, config))
                        .category(buildAutoSilkCategory(defaults, config))
                        .category(buildChatAppendCategory(defaults, config))
                        .category(buildChatFiltersCategory(defaults, config))
                        .category(buildChatLoggerCategory(defaults, config))
                        .category(buildEnchantAllCategory(defaults, config))
                        .category(buildEventNotifierCategory(defaults, config))
                        .category(buildJoinCommandsCategory(defaults, config))
                        .category(buildMentionsCategory(defaults, config))
                        .category(buildNoGMTCategory(defaults, config))
                        .category(buildStaffDetectorCategory(defaults, config))
                        .category(buildToolSaverCategory(defaults, config))
                        .save(ModConfig.HANDLER::save))
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
                                        defaults.prefixFormat,
                                        () -> config.prefixFormat,
                                        (value) -> config.prefixFormat = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.sbutilsColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.sbutilsColor.tooltip")))
                                .binding(
                                        defaults.sbutilsColor,
                                        () -> config.sbutilsColor,
                                        (value) -> config.sbutilsColor = value
                                )
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.prefixColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.prefixColor.tooltip")))
                                .binding(
                                        defaults.prefixColor,
                                        () -> config.prefixColor,
                                        (value) -> config.prefixColor = value
                                )
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.messageColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.messageColor.tooltip")))
                                .binding(
                                        defaults.messageColor,
                                        () -> config.messageColor,
                                        (value) -> config.messageColor = value
                                )
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.valueColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.valueColor.tooltip")))
                                .binding(
                                        defaults.valueColor,
                                        () -> config.valueColor,
                                        (value) -> config.valueColor = value
                                )
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    public static ConfigCategory buildAntiPlaceCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.antiPlace"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.antiPlace"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.antiPlace.heads"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.antiPlace.heads.tooltip")))
                                .binding(
                                        defaults.antiPlace.heads,
                                        () -> config.antiPlace.heads,
                                        (value) -> config.antiPlace.heads = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.antiPlace.grass"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.antiPlace.grass")))
                                .binding(
                                        defaults.antiPlace.grass,
                                        () -> config.antiPlace.grass,
                                        (value) -> config.antiPlace.grass = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoAdvertCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoAdvert"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoAdvert"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.enabled.tooltip")))
                                .binding(
                                        defaults.autoAdvert.enabled,
                                        () -> config.autoAdvert.enabled,
                                        (value) -> config.autoAdvert.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert.sbFile"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.sbFile.tooltip")))
                                .binding(
                                        defaults.autoAdvert.sbFile,
                                        () -> config.autoAdvert.sbFile,
                                        (value) -> config.autoAdvert.sbFile = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert.ecoFile"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.ecoFile.tooltip")))
                                .binding(
                                        defaults.autoAdvert.ecoFile,
                                        () -> config.autoAdvert.ecoFile,
                                        (value) -> config.autoAdvert.ecoFile = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert.classicFile"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.classicFile.tooltip")))
                                .binding(
                                        defaults.autoAdvert.classicFile,
                                        () -> config.autoAdvert.classicFile,
                                        (value) -> config.autoAdvert.classicFile = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert.delay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.delay.tooltip")))
                                .binding(
                                        defaults.autoAdvert.delay,
                                        () -> config.autoAdvert.delay,
                                        (value) -> config.autoAdvert.delay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert.initialDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.initialDelay.tooltip")))
                                .binding(
                                        defaults.autoAdvert.initialDelay,
                                        () -> config.autoAdvert.initialDelay,
                                        (value) -> config.autoAdvert.initialDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoAdvert.useWhitelist"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.useWhitelist.tooltip")))
                                .binding(
                                        defaults.autoAdvert.useWhitelist,
                                        () -> config.autoAdvert.useWhitelist,
                                        (value) -> config.autoAdvert.useWhitelist = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.autoAdvert.whitelist"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoAdvert.whitelist.tooltip")))
                        .binding(
                                defaults.autoAdvert.whitelist,
                                () -> config.autoAdvert.whitelist,
                                (value) -> config.autoAdvert.whitelist = value
                        )
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    public static ConfigCategory buildAutoCommandCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoCommand"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoCommand"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCommand.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCommand.enabled.tooltip")))
                                .binding(
                                        defaults.autoCommand.enabled,
                                        () -> config.autoCommand.enabled,
                                        (value) -> config.autoCommand.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCommand.minDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCommand.minDelay.tooltip")))
                                .binding(
                                        defaults.autoCommand.minDelay,
                                        () -> config.autoCommand.minDelay,
                                        (value) -> config.autoCommand.minDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<ModConfig.AutoCommandConfig.AutoCommandEntry>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.autoCommand.commands"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCommand.commands.tooltip")))
                        .insertEntriesAtEnd(true)
                        .binding(
                                defaults.autoCommand.commands,
                                () -> config.autoCommand.commands,
                                (value) -> config.autoCommand.commands = value
                        )
                        .customController(AutoCommandEntryController::new)
                        .initial(new ModConfig.AutoCommandConfig.AutoCommandEntry("", 1.0, false))
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoCrateCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoCrate"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoCrate"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCrate.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCrate.enabled.tooltip")))
                                .binding(
                                        defaults.autoCrate.enabled,
                                        () -> config.autoCrate.enabled,
                                        (value) -> config.autoCrate.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.Crate>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCrate.mode"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCrate.mode.tooltip")))
                                .binding(
                                        defaults.autoCrate.mode,
                                        () -> config.autoCrate.mode,
                                        (value) -> config.autoCrate.mode = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.Crate.class))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCrate.delay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCrate.delay.tooltip")))
                                .binding(
                                        defaults.autoCrate.delay,
                                        () -> config.autoCrate.delay,
                                        (value) -> config.autoCrate.delay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCrate.distance"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCrate.distance.tooltip")))
                                .binding(
                                        defaults.autoCrate.distance,
                                        () -> config.autoCrate.distance,
                                        (value) -> config.autoCrate.distance = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoCrate.cleaner"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCrate.cleaner.tooltip")))
                                .binding(
                                        defaults.autoCrate.cleaner,
                                        () -> config.autoCrate.cleaner,
                                        (value) -> config.autoCrate.cleaner = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.autoCrate.itemsToClean"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoCrate.itemsToClean.tooltip")))
                        .binding(
                                defaults.autoCrate.itemsToClean,
                                () -> config.autoCrate.itemsToClean,
                                (value) -> config.autoCrate.itemsToClean = value
                        )
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoFixCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoFix"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoFix"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFix.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFix.enabled.tooltip")))
                                .binding(
                                        defaults.autoFix.enabled,
                                        () -> config.autoFix.enabled,
                                        (value) -> config.autoFix.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.FixMode>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFix.mode"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFix.mode.tooltip")))
                                .binding(
                                        defaults.autoFix.mode,
                                        () -> config.autoFix.mode,
                                        (value) -> config.autoFix.mode = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.FixMode.class))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFix.percent"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFix.percent.tooltip")))
                                .binding(
                                        defaults.autoFix.percent,
                                        () -> config.autoFix.percent,
                                        (value) -> config.autoFix.percent = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .listener(((doubleOption, aDouble) -> AutoFix.onChangeMaxFixPercent()))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFix.delay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFix.delay.tooltip")))
                                .binding(
                                        defaults.autoFix.delay,
                                        () -> config.autoFix.delay,
                                        (value) -> config.autoFix.delay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFix.retryDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFix.retryDelay.tooltip")))
                                .binding(
                                        defaults.autoFix.retryDelay,
                                        () -> config.autoFix.retryDelay,
                                        (value) -> config.autoFix.retryDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoFix.maxRetries"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoFix.maxRetries.tooltip")))
                                .binding(
                                        defaults.autoFix.maxRetries,
                                        () -> config.autoFix.maxRetries,
                                        (value) -> config.autoFix.maxRetries = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoKitCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoKit"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoKit"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoKit.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoKit.enabled.tooltip")))
                                .binding(
                                        defaults.autoKit.enabled,
                                        () -> config.autoKit.enabled,
                                        (value) -> config.autoKit.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoKit.commandDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoKit.commandDelay.tooltip")))
                                .binding(
                                        defaults.autoKit.commandDelay,
                                        () -> config.autoKit.commandDelay,
                                        (value) -> config.autoKit.commandDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoKit.claimDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoKit.claimDelay.tooltip")))
                                .binding(
                                        defaults.autoKit.claimDelay,
                                        () -> config.autoKit.claimDelay,
                                        (value) -> config.autoKit.claimDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoKit.systemDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoKit.systemDelay.tooltip")))
                                .binding(
                                        defaults.autoKit.systemDelay,
                                        () -> config.autoKit.systemDelay,
                                        (value) -> config.autoKit.systemDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<ModConfig.Kit>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.autoKit.kits"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoKit.kits.tooltip")))
                        .insertEntriesAtEnd(true)
                        .binding(
                                defaults.autoKit.kits,
                                () -> config.autoKit.kits,
                                (value) -> config.autoKit.kits = new ArrayList<>(new LinkedHashSet<>(value))
                        )
                        .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.Kit.class))
                        .initial(ModConfig.Kit.SKYTITAN)
                        .listener(((listOption, kits) -> AutoKit.onKitListChanged()))
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoMineCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoMine"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoMine"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoMine.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoMine.enabled.tooltip")))
                                .binding(
                                        defaults.autoMine.enabled,
                                        () -> config.autoMine.enabled,
                                        (value) -> config.autoMine.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoMine.autoSwitch"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoMine.autoSwitch.tooltip")))
                                .binding(
                                        defaults.autoMine.autoSwitch,
                                        () -> config.autoMine.autoSwitch,
                                        (value) -> config.autoMine.autoSwitch = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoMine.switchDurability"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoMine.switchDurability.tooltip")))
                                .binding(
                                        defaults.autoMine.switchDurability,
                                        () -> config.autoMine.switchDurability,
                                        (value) -> config.autoMine.switchDurability = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoPrivateCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoPrivate"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoPrivate"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoPrivate.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoPrivate.enabled.tooltip")))
                                .binding(
                                        defaults.autoPrivate.enabled,
                                        () -> config.autoPrivate.enabled,
                                        (value) -> config.autoPrivate.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.autoPrivate.names"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoPrivate.names.tooltip")))
                        .maximumNumberOfEntries(2)
                        .insertEntriesAtEnd(true)
                        .binding(
                                defaults.autoPrivate.names,
                                () -> config.autoPrivate.names,
                                (value) -> config.autoPrivate.names = new ArrayList<>(new LinkedHashSet<>(value))
                        )
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoRaffleCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoRaffle"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoRaffle"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoRaffle.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoRaffle.enabled.tooltip")))
                                .binding(
                                        defaults.autoRaffle.enabled,
                                        () -> config.autoRaffle.enabled,
                                        (value) -> config.autoRaffle.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoRaffle.sbTickets"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoRaffle.sbTickets.tooltip")))
                                .binding(
                                        defaults.autoRaffle.sbTickets,
                                        () -> config.autoRaffle.sbTickets,
                                        (value) -> config.autoRaffle.sbTickets = value
                                )
                                .customController(integerOption -> new IntegerSliderController(integerOption, 1, 2, 1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoRaffle.ecoTickets"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoRaffle.ecoTickets.tooltip")))
                                .binding(
                                        defaults.autoRaffle.ecoTickets,
                                        () -> config.autoRaffle.ecoTickets,
                                        (value) -> config.autoRaffle.ecoTickets = value
                                )
                                .customController(integerOption -> new IntegerSliderController(integerOption, 1, 2, 1))
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoReplyCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoReply"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoReply"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoReply.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoReply.enabled.tooltip")))
                                .binding(
                                        defaults.autoReply.enabled,
                                        () -> config.autoReply.enabled,
                                        (value) -> config.autoReply.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoReply.response"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoReply.response.tooltip")))
                                .binding(
                                        defaults.autoReply.response,
                                        () -> config.autoReply.response,
                                        (value) -> config.autoReply.response = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoReply.delay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoReply.delay.tooltip")))
                                .binding(
                                        defaults.autoReply.delay,
                                        () -> config.autoReply.delay,
                                        (value) -> config.autoReply.delay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildAutoSilkCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.autoSilk"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.autoSilk"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilk.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilk.enabled.tooltip")))
                                .binding(
                                        defaults.autoSilk.enabled,
                                        () -> config.autoSilk.enabled,
                                        (value) -> config.autoSilk.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.SilkTarget>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilk.targetTool"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilk.targetTool.tooltip")))
                                .binding(
                                        defaults.autoSilk.targetTool,
                                        () -> config.autoSilk.targetTool,
                                        (value) -> config.autoSilk.targetTool = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.SilkTarget.class))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilk.cleaner"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilk.cleaner.tooltip")))
                                .binding(
                                        defaults.autoSilk.cleaner,
                                        () -> config.autoSilk.cleaner,
                                        (value) -> config.autoSilk.cleaner = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilk.delay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilk.delay.tooltip")))
                                .binding(
                                        defaults.autoSilk.delay,
                                        () -> config.autoSilk.delay,
                                        (value) -> config.autoSilk.delay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilk.showButton"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilk.showButton.tooltip")))
                                .binding(
                                        defaults.autoSilk.showButton,
                                        () -> config.autoSilk.showButton,
                                        (value) -> config.autoSilk.showButton = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.CornerButtonPos>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.autoSilk.buttonPos"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.autoSilk.buttonPos.tooltip")))
                                .binding(
                                        defaults.autoSilk.buttonPos,
                                        () -> config.autoSilk.buttonPos,
                                        (value) -> config.autoSilk.buttonPos = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.CornerButtonPos.class))
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatAppendCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatAppend"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatAppend"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatAppend.addPrefix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatAppend.addPrefix.tooltip")))
                                .binding(
                                        defaults.chatAppend.addPrefix,
                                        () -> config.chatAppend.addPrefix,
                                        (value) -> config.chatAppend.addPrefix = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatAppend.prefix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatAppend.prefix.tooltip")))
                                .binding(
                                        defaults.chatAppend.prefix,
                                        () -> config.chatAppend.prefix,
                                        (value) -> config.chatAppend.prefix = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatAppend.addSuffix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatAppend.addSuffix.tooltip")))
                                .binding(
                                        defaults.chatAppend.addSuffix,
                                        () -> config.chatAppend.addSuffix,
                                        (value) -> config.chatAppend.addSuffix = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatAppend.suffix"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatAppend.suffix.tooltip")))
                                .binding(
                                        defaults.chatAppend.suffix,
                                        () -> config.chatAppend.suffix,
                                        (value) -> config.chatAppend.suffix = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatFiltersCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatFilters"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatFilters"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.tipsFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.tipsFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.tipsFilter,
                                        () -> config.chatFilters.tipsFilter,
                                        (value) -> config.chatFilters.tipsFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.advancementsFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.advancementsFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.advancementsFilter,
                                        () -> config.chatFilters.advancementsFilter,
                                        (value) -> config.chatFilters.advancementsFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.welcomeFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.welcomeFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.welcomeFilter,
                                        () -> config.chatFilters.welcomeFilter,
                                        (value) -> config.chatFilters.welcomeFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.friendJoinFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.friendJoinFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.friendJoinFilter,
                                        () -> config.chatFilters.friendJoinFilter,
                                        (value) -> config.chatFilters.friendJoinFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.motdFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.motdFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.motdFilter,
                                        () -> config.chatFilters.motdFilter,
                                        (value) -> config.chatFilters.motdFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.voteFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.voteFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.voteFilter,
                                        () -> config.chatFilters.voteFilter,
                                        (value) -> config.chatFilters.voteFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.voteRewardFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.voteRewardFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.voteRewardFilter,
                                        () -> config.chatFilters.voteRewardFilter,
                                        (value) -> config.chatFilters.voteRewardFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.raffleFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.raffleFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.raffleFilter,
                                        () -> config.chatFilters.raffleFilter,
                                        (value) -> config.chatFilters.raffleFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.cratesFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.cratesFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.cratesFilter,
                                        () -> config.chatFilters.cratesFilter,
                                        (value) -> config.chatFilters.cratesFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.perishedInVoidFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.perishedInVoidFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.perishedInVoidFilter,
                                        () -> config.chatFilters.perishedInVoidFilter,
                                        (value) -> config.chatFilters.perishedInVoidFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatFilters.skyChatFilter"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatFilters.skyChatFilter.tooltip")))
                                .binding(
                                        defaults.chatFilters.skyChatFilter,
                                        () -> config.chatFilters.skyChatFilter,
                                        (value) -> config.chatFilters.skyChatFilter = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildChatLoggerCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.chatLogger"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.chatLogger"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatLogger.shopIncoming"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatLogger.shopIncoming.tooltip")))
                                .binding(
                                        defaults.chatLogger.shopIncoming,
                                        () -> config.chatLogger.shopIncoming,
                                        (value) -> config.chatLogger.shopIncoming = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatLogger.shopOutgoing"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatLogger.shopOutgoing.tooltip")))
                                .binding(
                                        defaults.chatLogger.shopOutgoing,
                                        () -> config.chatLogger.shopOutgoing,
                                        (value) -> config.chatLogger.shopOutgoing = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatLogger.msgIncoming"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatLogger.msgIncoming.tooltip")))
                                .binding(
                                        defaults.chatLogger.msgIncoming,
                                        () -> config.chatLogger.msgIncoming,
                                        (value) -> config.chatLogger.msgIncoming = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatLogger.msgOutgoing"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatLogger.msgOutgoing.tooltip")))
                                .binding(
                                        defaults.chatLogger.msgOutgoing,
                                        () -> config.chatLogger.msgOutgoing,
                                        (value) -> config.chatLogger.msgOutgoing = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatLogger.visits"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatLogger.visits.tooltip")))
                                .binding(
                                        defaults.chatLogger.visits,
                                        () -> config.chatLogger.visits,
                                        (value) -> config.chatLogger.visits = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.chatLogger.dp"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.chatLogger.dp.tooltip")))
                                .binding(
                                        defaults.chatLogger.dp,
                                        () -> config.chatLogger.dp,
                                        (value) -> config.chatLogger.dp = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildEnchantAllCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.enchantAll"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.enchantAll"))
                        .option(Option.<ModConfig.EnchantMode>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.enchantAll.mode"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.enchantAll.mode.tooltip")))
                                .binding(
                                        defaults.enchantAll.mode,
                                        () -> config.enchantAll.mode,
                                        (value) -> config.enchantAll.mode = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.EnchantMode.class))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.enchantAll.delay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.enchantAll.delay.tooltip")))
                                .binding(
                                        defaults.enchantAll.delay,
                                        () -> config.enchantAll.delay,
                                        (value) -> config.enchantAll.delay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.enchantAll.cooldownFrequency"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.enchantAll.cooldownFrequency.tooltip")))
                                .binding(
                                        defaults.enchantAll.cooldownFrequency,
                                        () -> config.enchantAll.cooldownFrequency,
                                        (value) -> config.enchantAll.cooldownFrequency = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.enchantAll.cooldownTime"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.enchantAll.cooldownTime.tooltip")))
                                .binding(
                                        defaults.enchantAll.cooldownTime,
                                        () -> config.enchantAll.cooldownTime,
                                        (value) -> config.enchantAll.cooldownTime = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.enchantAll.excludeFrost"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.enchantAll.excludeFrost.tooltip")))
                                .binding(
                                        defaults.enchantAll.excludeFrost,
                                        () -> config.enchantAll.excludeFrost,
                                        (value) -> config.enchantAll.excludeFrost = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildEventNotifierCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.eventNotifier"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.eventNotifier"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.eventNotifier.llamaSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.eventNotifier.showLlamaTitle.tooltip")))
                                .binding(
                                        defaults.eventNotifier.showLlamaTitle,
                                        () -> config.eventNotifier.showLlamaTitle,
                                        (value) -> config.eventNotifier.showLlamaTitle = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.eventNotifier.playLlamaSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.eventNotifier.playLlamaSound.tooltip")))
                                .binding(
                                        defaults.eventNotifier.playLlamaSound,
                                        () -> config.eventNotifier.playLlamaSound,
                                        (value) -> config.eventNotifier.playLlamaSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.eventNotifier.llamaSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.eventNotifier.llamaSound.tooltip")))
                                .binding(
                                        defaults.eventNotifier.llamaSound,
                                        () -> config.eventNotifier.llamaSound,
                                        (value) -> config.eventNotifier.llamaSound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.eventNotifier.showTraderTitle"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.eventNotifier.showTraderTitle.tooltip")))
                                .binding(
                                        defaults.eventNotifier.showTraderTitle,
                                        () -> config.eventNotifier.showTraderTitle,
                                        (value) -> config.eventNotifier.showTraderTitle = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.eventNotifier.playTraderSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.eventNotifier.playTraderSound.tooltip")))
                                .binding(
                                        defaults.eventNotifier.playTraderSound,
                                        () -> config.eventNotifier.playTraderSound,
                                        (value) -> config.eventNotifier.playTraderSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.eventNotifier.traderSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.eventNotifier.traderSound.tooltip")))
                                .binding(
                                        defaults.eventNotifier.traderSound,
                                        () -> config.eventNotifier.traderSound,
                                        (value) -> config.eventNotifier.traderSound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildJoinCommandsCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.joinCommands"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.joinCommands"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.joinCommands.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.joinCommands.enabled.tooltip")))
                                .binding(
                                        defaults.joinCommands.enabled,
                                        () -> config.joinCommands.enabled,
                                        (value) -> config.joinCommands.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.joinCommands.initialDelay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.joinCommands.initialDelay.tooltip")))
                                .binding(
                                        defaults.joinCommands.initialDelay,
                                        () -> config.joinCommands.initialDelay,
                                        (value) -> config.joinCommands.initialDelay = value
                                )
                                .controller(DoubleFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.joinCommands.delay"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.joinCommands.delay.tooltip")))
                                .binding(
                                        defaults.joinCommands.delay,
                                        () -> config.joinCommands.delay,
                                        (value) -> config.joinCommands.delay = value
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
                                .name(Text.translatable("text.sbutils.config.option.mentions.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.enabled.tooltip")))
                                .binding(
                                        defaults.mentions.enabled,
                                        () -> config.mentions.enabled,
                                        (value) -> config.mentions.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.playSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.playSound.tooltip")))
                                .binding(
                                        defaults.mentions.playSound,
                                        () -> config.mentions.playSound,
                                        (value) -> config.mentions.playSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.sound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.sound.tooltip")))
                                .binding(
                                        defaults.mentions.sound,
                                        () -> config.mentions.sound,
                                        (value) -> config.mentions.sound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.highlight"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.highlight.tooltip")))
                                .binding(
                                        defaults.mentions.highlight,
                                        () -> config.mentions.highlight,
                                        (value) -> config.mentions.highlight = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.highlightColor"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.highlightColor.tooltip")))
                                .binding(
                                        defaults.mentions.highlightColor,
                                        () -> config.mentions.highlightColor,
                                        (value) -> config.mentions.highlightColor = value
                                )
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.excludeServerMsgs"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.excludeServerMsgs.tooltip")))
                                .binding(
                                        defaults.mentions.excludeServerMsgs,
                                        () -> config.mentions.excludeServerMsgs,
                                        (value) -> config.mentions.excludeServerMsgs = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.excludeSelfMsgs"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.excludeSelfMsgs.tooltip")))
                                .binding(
                                        defaults.mentions.excludeSelfMsgs,
                                        () -> config.mentions.excludeSelfMsgs,
                                        (value) -> config.mentions.excludeSelfMsgs = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.excludeSender"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.excludeSender.tooltip")))
                                .binding(
                                        defaults.mentions.excludeSender,
                                        () -> config.mentions.excludeSender,
                                        (value) -> config.mentions.excludeSender = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.mentions.currentAccount"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.currentAccount.tooltip")))
                                .binding(
                                        defaults.mentions.currentAccount,
                                        () -> config.mentions.currentAccount,
                                        (value) -> config.mentions.currentAccount = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Text.translatable("text.sbutils.config.option.mentions.aliases"))
                        .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.mentions.aliases.tooltip")))
                        .binding(
                                defaults.mentions.aliases,
                                () -> config.mentions.aliases,
                                (value) -> config.mentions.aliases = value
                        )
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildNoGMTCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.noGmt"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.noGmt"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.noGmt.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.noGmt.enabled.tooltip")))
                                .binding(
                                        defaults.noGmt.enabled,
                                        () -> config.noGmt.enabled,
                                        (value) -> config.noGmt.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.noGmt.timeZone"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.noGmt.timeZone.tooltip")))
                                .binding(
                                        defaults.noGmt.timeZone,
                                        () -> config.noGmt.timeZone,
                                        (value) -> config.noGmt.timeZone = value
                                )
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.noGmt.showTimeZone"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.noGmt.showTimeZone.tooltip")))
                                .binding(
                                        defaults.noGmt.showTimeZone,
                                        () -> config.noGmt.showTimeZone,
                                        (value) -> config.noGmt.showTimeZone = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildStaffDetectorCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.staffDetector"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.staffDetector"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.staffDetector.detectJoin"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.staffDetector.detectJoin.tooltip")))
                                .binding(
                                        defaults.staffDetector.detectJoin,
                                        () -> config.staffDetector.detectJoin,
                                        (value) -> config.staffDetector.detectJoin = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.staffDetector.detectLeave"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.staffDetector.detectLeave.tooltip")))
                                .binding(
                                        defaults.staffDetector.detectLeave,
                                        () -> config.staffDetector.detectLeave,
                                        (value) -> config.staffDetector.detectLeave = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.staffDetector.playSound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.staffDetector.playSound.tooltip")))
                                .binding(
                                        defaults.staffDetector.playSound,
                                        () -> config.staffDetector.playSound,
                                        (value) -> config.staffDetector.playSound = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<ModConfig.NotifSound>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.staffDetector.sound"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.staffDetector.sound.tooltip")))
                                .binding(
                                        defaults.staffDetector.sound,
                                        () -> config.staffDetector.sound,
                                        (value) -> config.staffDetector.sound = value
                                )
                                .controller(option -> EnumControllerBuilder.create(option).enumClass(ModConfig.NotifSound.class))
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildToolSaverCategory(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.sbutils.config.category.toolSaver"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("text.sbutils.config.group.toolSaver"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.toolSaver.enabled"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.toolSaver.enabled.tooltip")))
                                .binding(
                                        defaults.toolSaver.enabled,
                                        () -> config.toolSaver.enabled,
                                        (value) -> config.toolSaver.enabled = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("text.sbutils.config.option.toolSaver.durability"))
                                .description(OptionDescription.of(Text.translatable("text.sbutils.config.option.toolSaver.durability.tooltip")))
                                .binding(
                                        defaults.toolSaver.durability,
                                        () -> config.toolSaver.durability,
                                        (value) -> config.toolSaver.durability = value
                                )
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }
}
