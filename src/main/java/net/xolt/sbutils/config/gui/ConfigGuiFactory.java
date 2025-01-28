package net.xolt.sbutils.config.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.Constraints;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.NumberConstraints;
import net.xolt.sbutils.config.gui.controllers.AutoCommandEntryController;
import net.xolt.sbutils.config.gui.controllers.FilterEntryController;
import net.xolt.sbutils.config.gui.controllers.JoinCommandsEntryController;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.feature.Features;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class ConfigGuiFactory<C> {

    private final String key;
    private final Features<C> features;
    private final List<ConfigBinding<C, ?>> globalSettings;

    public ConfigGuiFactory(String key, Features<C> features, List<ConfigBinding<C, ?>> globalSettings) {
        this.features = features;
        this.key = key;
        this.globalSettings = globalSettings;
    }

    public Screen getConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(features.getConfigHandler(), (defaults, config, builder) -> {
                    builder.title(Component.translatable("text." + key + ".config.title"));

                    if (!globalSettings.isEmpty())
                        builder.category(buildGlobalCategory(defaults, config));

                    return builder.categories(buildFeatureCategories(features, defaults, config)).save(features.getConfigHandler()::save);
                }
                ).generateScreen(parent);
    }

    private ConfigCategory buildGlobalCategory(C defaults, C config) {
        return buildCategory(Component.translatable("text." + key + ".config.category.default"), Component.translatable("text." + key + ".config.group." + key), globalSettings, defaults, config);
    }

    public List<ConfigCategory> buildFeatureCategories(Features<C> features, C defaults, C config) {
        return features.getAll().stream().filter((feature) -> feature.getConfigBindings() != null).map((feature) -> buildFeatureCategory(feature, defaults, config)).toList();
    }

    public static <C> ConfigCategory buildFeatureCategory(Feature<C> feature, C defaults, C config) {
        return buildCategory(feature.getName(), feature.getGroupName(), feature.getConfigBindings(), defaults, config);
    }

    private static <C> ConfigCategory buildCategory(MutableComponent name, MutableComponent groupName, List<? extends ConfigBinding<C, ?>> configBindings, C defaults, C config) {
        ConfigCategory.Builder builder = ConfigCategory.createBuilder()
                .name(name);
        OptionGroup.Builder optionGroup = OptionGroup.createBuilder()
                .name(groupName);

        for (ConfigBinding<C, ?> binding : configBindings) {
            if (!(binding instanceof OptionBinding<C, ?> optionBinding))
                continue;
            Option<?> option = buildOption(optionBinding, defaults, config);
            if (option != null)
                optionGroup = optionGroup.option(option);
            else
                SbUtils.LOGGER.error("Failed to build option \"" + binding.getPath() + "\"");
        }

        builder = builder.group(optionGroup.build());

        for (ConfigBinding<C, ?> binding : configBindings) {
            if (!(binding instanceof ListOptionBinding listOptionBinding))
                continue;
            ListOption<?> option = buildListOption(listOptionBinding, defaults, config);
            if (option != null)
                builder = builder.group(option);
            else
                SbUtils.LOGGER.error("Failed to build list option \"" + binding.getPath() + "\"");
        }

        return builder.build();
    }

    private static <C, T> Option<T> buildOption(OptionBinding<C, T> binding, C defaults, C config) {
        Function<Option<T>, Controller<T>> controller = getControllerFor(binding.getType(), binding.getConstraints());
        if (controller == null)
            return null;

        return Option.<T>createBuilder()
                .name(binding.getName())
                .description(OptionDescription.of(binding.getTooltip()))
                .binding(
                        binding.get(defaults),
                        () -> binding.get(config),
                        (value) -> binding.set(config, value)
                )
                .customController(controller)
                .build();
    }

    private static <C, T> ListOption<T> buildListOption(ListOptionBinding<C, T> binding, C defaults, C config) {
        Constraints<List<T>> constraints = binding.getConstraints();
        int maxSize = Integer.MAX_VALUE;
        final Constraints<T> entryConstraints;
        if (constraints instanceof ListConstraints<T> listConstraints) {
            maxSize = listConstraints.getMaxSize();
            entryConstraints = listConstraints.getEntryConstraints();
        } else {
            entryConstraints = null;
        }

        Function<Option<T>, Controller<T>> controller = getControllerFor(binding.getListType(), entryConstraints);
        if (controller == null)
            return null;

        return ListOption.<T>createBuilder()
                .name(binding.getName())
                .description(OptionDescription.of(binding.getTooltip()))
                .binding(
                        binding.get(defaults),
                        () -> binding.get(config),
                        (value) -> binding.set(config, value)
                )
                .maximumNumberOfEntries(maxSize)
                .customController(controller::apply)
                .initial(binding.getInitialValue())
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<Option<T>, Controller<T>> getControllerFor(Class<T> type, Constraints<T> constraints) {
        if (type.equals(Integer.class)) {
            Number min;
            Number max;
            if (constraints instanceof NumberConstraints<?> numConstraints) {
                min = numConstraints.getMin();
                max = numConstraints.getMax();
            } else {
                min = Integer.MIN_VALUE;
                max = Integer.MAX_VALUE;
            }

            int minimum = min == null ? Integer.MIN_VALUE : min.intValue();
            int maximum = max == null ? Integer.MAX_VALUE : max.intValue();

            if (min != null && max != null)
                return (option) -> (Controller<T>)IntegerSliderControllerBuilder.create((Option<Integer>)option).range(minimum, maximum).step(1).build();
            return (option) -> (Controller<T>)IntegerFieldControllerBuilder.create((Option<Integer>)option).min(minimum).max(maximum).build();
        } else if (type.equals(Double.class)) {
            Number min = null;
            Number max = null;
            if (constraints instanceof NumberConstraints<?> numConstraints) {
                min = numConstraints.getMin();
                max = numConstraints.getMax();
            }
            double minimum = min == null ? Integer.MIN_VALUE : min.doubleValue();
            double maximum = max == null ? Integer.MAX_VALUE : max.doubleValue();
            if (min != null && max != null)
                return (option) -> (Controller<T>)DoubleSliderControllerBuilder.create((Option<Double>)option).range(minimum, maximum).step(0.1).build();
            return (option) -> (Controller<T>)DoubleFieldControllerBuilder.create((Option<Double>)option).min(minimum).max(maximum).build();
        } else if (type.equals(Boolean.class)) {
            return (option) -> (Controller<T>)TickBoxControllerBuilder.create((Option<Boolean>) option).build();
        }
        else if (Enum.class.isAssignableFrom(type)) {
            return (option) -> (Controller<T>)EnumControllerBuilder.create((Option<Enum>)option).enumClass((Class<Enum>)type).build();
        } else if (type.equals(String.class)) {
            return (option) -> (Controller<T>)StringControllerBuilder.create((Option<String>) option).build();
        } else if (type.equals(Color.class)) {
            return (option) -> (Controller<T>)ColorControllerBuilder.create((Option<Color>) option).build();
        } else if (type.equals(ModConfig.AutoCommandConfig.AutoCommandEntry.class)) {
            return (option) -> (Controller<T>)new AutoCommandEntryController((Option<ModConfig.AutoCommandConfig.AutoCommandEntry>) option);
        } else if (type.equals(ModConfig.JoinCommandsConfig.JoinCommandsEntry.class)) {
            return (option) -> (Controller<T>)new JoinCommandsEntryController((Option<ModConfig.JoinCommandsConfig.JoinCommandsEntry>) option);
        } else if (type.equals(ModConfig.ChatFiltersConfig.FilterEntry.class)) {
            return (option) -> (Controller<T>)new FilterEntryController((Option<ModConfig.ChatFiltersConfig.FilterEntry>)option);
        }
        return null;
    }
}
