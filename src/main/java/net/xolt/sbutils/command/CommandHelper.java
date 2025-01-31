package net.xolt.sbutils.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.xolt.sbutils.command.argument.GenericEnumArgumentType;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.Constraints;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.NumberConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandHelper {

    public static LiteralArgumentBuilder<FabricClientCommandSource> runnable(String command, Runnable runnable) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    runnable.run();
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static <C> LiteralArgumentBuilder<FabricClientCommandSource> toggle(String command, Feature<C> feature, OptionBinding<C, Boolean> optionBinding, ConfigClassHandler<C> configHandler) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    optionBinding.set(configHandler.instance(), !optionBinding.get(configHandler.instance()));
                    ModConfig.HANDLER.save();
                    ChatUtils.printChangedSetting(feature.getNameTranslation(), optionBinding.get(configHandler.instance()));
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static <C> LiteralArgumentBuilder<FabricClientCommandSource> string(String command, String unit, OptionBinding<C, String> optionBinding, ConfigClassHandler<C> configHandler) {
        boolean greedy = true;
        if (optionBinding.getConstraints() instanceof StringConstraints strConstraints)
            greedy = strConstraints.getSpacesAllowed() == null || strConstraints.getSpacesAllowed();

        return getter(command, optionBinding, configHandler)
                .then(ClientCommandManager.literal("set")
                        .executes(context -> {
                            optionBinding.set(configHandler.instance(), "");
                            ModConfig.HANDLER.save();
                            ChatUtils.printChangedSetting(optionBinding.getTranslation(), "");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(setter(unit, optionBinding, configHandler, greedy ? StringArgumentType.greedyString() : StringArgumentType.string(), StringArgumentType::getString)));

    }

    public static <C> LiteralArgumentBuilder<FabricClientCommandSource> stringList(String command, String unit, ListOptionBinding<C, String> optionBinding, ConfigClassHandler<C> configHandler) {
        boolean greedy = true;
        if (optionBinding.getConstraints() instanceof ListConstraints<String> listConstraints && listConstraints.getEntryConstraints() instanceof StringConstraints strConstraints)
            greedy = strConstraints.getSpacesAllowed() == null || strConstraints.getSpacesAllowed();

        return genericList(command, unit, optionBinding, configHandler, greedy, greedy ? StringArgumentType.greedyString() : StringArgumentType.string(), StringArgumentType::getString);
    }

    public static <C, T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> enumList(String command, String unit, ListOptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler) {
        return enumList(command, unit, optionBinding, configHandler, false);
    }

    public static <C, T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> enumList(String command, String unit, ListOptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler, boolean indexed) {
        return genericList(command, unit, optionBinding, configHandler, indexed, GenericEnumArgumentType.genericEnum(optionBinding.getListType()), (context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, optionBinding.getListType()));
    }



    public static <C, T> LiteralArgumentBuilder<FabricClientCommandSource> genericList(String command, String unit, ListOptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return genericList(command, unit, optionBinding, configHandler, false, argumentType, getArgument);
    }

    public static <C, T> LiteralArgumentBuilder<FabricClientCommandSource> genericList(String command, String unit, ListOptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler, boolean indexed, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        final int maxSize;
        final boolean allowDupes;
        if (optionBinding.getConstraints() instanceof ListConstraints<T> listConstraints) {
            maxSize = listConstraints.getMaxSize();
            allowDupes = listConstraints.getAllowDupes();
        } else {
            maxSize = Integer.MAX_VALUE;
            allowDupes = false;
        }

        Consumer<T> add = (value) -> {
            ArrayList<T> list = new ArrayList<>(optionBinding.get(configHandler.instance()));

            if (list.size() >= maxSize) {
                ChatUtils.printWithPlaceholders("message.sbutils.listSizeError", maxSize, Component.translatable(optionBinding.getTranslation()));
                return;
            }

            if (!allowDupes && list.contains(value)) {
                ChatUtils.printWithPlaceholders("message.sbutils.listDupeError", value, Component.translatable(optionBinding.getTranslation()));
                return;
            }

            list.add(value);
            optionBinding.set(configHandler.instance(), list);
            ModConfig.HANDLER.save();
            ChatUtils.printWithPlaceholders("message.sbutils.listAddSuccess", value, Component.translatable(optionBinding.getTranslation()));
            ChatUtils.printListSetting(optionBinding.getTranslation(), list, indexed);
        };

        if (indexed)
            return customIndexedList(command, unit, optionBinding.getTranslation(), argumentType, getArgument, () -> optionBinding.get(configHandler.instance()), add,
                    (index) -> {
                        ArrayList<T> list = new ArrayList<>(optionBinding.get(configHandler.instance()));
                        int adjustedIndex = index - 1;
                        if (adjustedIndex >= list.size() || adjustedIndex < 0) {
                            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable(optionBinding.getTranslation()));
                            return;
                        }

                        T removed = list.remove(adjustedIndex);
                        optionBinding.set(configHandler.instance(), list);
                        ModConfig.HANDLER.save();
                        ChatUtils.printWithPlaceholders("message.sbutils.listDelSuccess", removed, Component.translatable(optionBinding.getTranslation()));
                        ChatUtils.printListSetting(optionBinding.getTranslation(), list, true);
                    },
                    (index, value) -> {
                        ArrayList<T> list = new ArrayList<>(optionBinding.get(configHandler.instance()));
                        int adjustedIndex = index - 1;
                        if (adjustedIndex >= list.size() || adjustedIndex < 0) {
                            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable(optionBinding.getTranslation()));
                            return;
                        }

                        if (list.size() >= maxSize) {
                            ChatUtils.printWithPlaceholders("message.sbutils.listSizeError", maxSize, Component.translatable(optionBinding.getTranslation()));
                            return;
                        }

                        if (!allowDupes && list.contains(value)) {
                            ChatUtils.printWithPlaceholders("message.sbutils.listDupeError", value, Component.translatable(optionBinding.getTranslation()));
                            return;
                        }

                        list.add(index, value);
                        optionBinding.set(configHandler.instance(), list);
                        ModConfig.HANDLER.save();
                        ChatUtils.printWithPlaceholders("message.sbutils.listAddSuccess", value, Component.translatable(optionBinding.getTranslation()));
                        ChatUtils.printListSetting(optionBinding.getTranslation(), list, true);
                    }
            );

        return customList(command, unit, optionBinding.getTranslation(), argumentType, getArgument, () -> optionBinding.get(configHandler.instance()), add,
                (value) -> {
                    ArrayList<T> list = new ArrayList<>(optionBinding.get(configHandler.instance()));
                    boolean result = list.remove(value);
                    optionBinding.set(configHandler.instance(), list);
                    ModConfig.HANDLER.save();
                    if (result) {
                        ChatUtils.printWithPlaceholders("message.sbutils.listDelSuccess", value, Component.translatable(optionBinding.getTranslation()));
                        ChatUtils.printListSetting(optionBinding.getTranslation(), list);
                    } else {
                        ChatUtils.printWithPlaceholders("message.sbutils.listDelFail", value, Component.translatable(optionBinding.getTranslation()));
                    }
                }
        );
    }

    public static <T, S> LiteralArgumentBuilder<FabricClientCommandSource> customList(String command, String unit, String translationKey, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<S>> getPrintable, Consumer<T> add, Consumer<T> del) {
        return runnable(command, () -> ChatUtils.printListSetting(translationKey, getPrintable.get()))
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument(unit, argumentType)
                                .executes(context -> {
                                    add.accept(getArgument.apply(context, unit));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("del")
                        .then(ClientCommandManager.argument(unit, argumentType)
                                .executes(context -> {
                                    del.accept(getArgument.apply(context, unit));
                                    return Command.SINGLE_SUCCESS;
                                })));
    }

    public static <T, S> LiteralArgumentBuilder<FabricClientCommandSource> customIndexedList(String command, String unit, String translationKey, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<S>> getPrintable, Consumer<T> add, Consumer<Integer> del, BiConsumer<Integer, T> insert) {
        return runnable(command, () -> ChatUtils.printListSetting(translationKey, getPrintable.get(), true))
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument(unit, argumentType)
                                .executes(context -> {
                                    add.accept(getArgument.apply(context, unit));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("del")
                        .then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    del.accept(IntegerArgumentType.getInteger(context, "index"));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("insert")
                        .then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                .then(ClientCommandManager.argument(unit, StringArgumentType.greedyString())
                                        .executes(context -> {
                                            insert.accept(IntegerArgumentType.getInteger(context, "index"), getArgument.apply(context, unit));
                                            return Command.SINGLE_SUCCESS;
                                        }))));
    }

    public static <C> LiteralArgumentBuilder<FabricClientCommandSource> bool(String command, OptionBinding<C, Boolean> optionBinding, ConfigClassHandler<C> configHandler) {
        return getterSetter(command, "enabled", optionBinding, configHandler, BoolArgumentType.bool(), BoolArgumentType::getBool);
    }

    public static <C> LiteralArgumentBuilder<FabricClientCommandSource> doubl(String command, String unit, OptionBinding<C, Double> optionBinding, ConfigClassHandler<C> configHandler) {
        return getterSetter(command, unit, optionBinding, configHandler, getDoubleArgumentType(optionBinding.getConstraints()), DoubleArgumentType::getDouble);
    }

    private static DoubleArgumentType getDoubleArgumentType(Constraints<Double> constraints) {
        if (!(constraints instanceof NumberConstraints<Double> numConstraints))
            return DoubleArgumentType.doubleArg();
        Double min = numConstraints.getMin();
        Double max = numConstraints.getMax();
        return DoubleArgumentType.doubleArg(min == null ? Double.MIN_VALUE : min, max == null ? Double.MAX_VALUE : max);
    }

    public static <C> LiteralArgumentBuilder<FabricClientCommandSource> integer(String command, String unit, OptionBinding<C, Integer> optionBinding, ConfigClassHandler<C> configHandler) {
        return getterSetter(command, unit, optionBinding, configHandler, IntegerArgumentType.integer(), IntegerArgumentType::getInteger);
    }

    public static <C, T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> genericEnum(String command, String unit, OptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler) {
        return getterSetter(command, unit, optionBinding, configHandler, GenericEnumArgumentType.genericEnum(optionBinding.getType()), ((context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, optionBinding.getType())));
    }

    public static <C, T> LiteralArgumentBuilder<FabricClientCommandSource> getterSetter(String command, String unit, OptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return getter(command, optionBinding, configHandler)
                .then(setter(unit, optionBinding, configHandler, argumentType, getArgument));
    }

    public static <C, T> LiteralArgumentBuilder<FabricClientCommandSource> getter(String command, OptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    ChatUtils.printSetting(optionBinding.getTranslation(), optionBinding.get(configHandler.instance()));
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static <C, T> RequiredArgumentBuilder<FabricClientCommandSource, T> setter(String argument, OptionBinding<C, T> optionBinding, ConfigClassHandler<C> configHandler, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return ClientCommandManager.argument(argument, argumentType)
                .executes(context -> {
                    optionBinding.set(configHandler.instance(), getArgument.apply(context, argument));
                    ModConfig.HANDLER.save();
                    ChatUtils.printChangedSetting(optionBinding.getTranslation(), optionBinding.get(configHandler.instance()));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
