package net.xolt.sbutils.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.xolt.sbutils.command.argument.GenericEnumArgumentType;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.Constraints;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.NumberConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandHelper {
    private static final String OPTION_KEY = "text.sbutils.config.option.";

    public static LiteralArgumentBuilder<FabricClientCommandSource> runnable(String command, Runnable runnable) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    runnable.run();
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> toggle(String command, Feature feature, OptionBinding<Boolean> optionBinding) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    optionBinding.set(ModConfig.HANDLER.instance(), !optionBinding.get(ModConfig.HANDLER.instance()));
                    ModConfig.HANDLER.save();
                    ChatUtils.printChangedSetting(feature.getNameTranslation(), optionBinding.get(ModConfig.HANDLER.instance()));
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> string(String command, String unit, OptionBinding<String> optionBinding) {
        boolean greedy = true;
        if (optionBinding.getConstraints() instanceof StringConstraints strConstraints)
            greedy = strConstraints.getSpacesAllowed() == null || strConstraints.getSpacesAllowed();

        return getter(command, optionBinding)
                .then(ClientCommandManager.literal("set")
                        .executes(context -> {
                            optionBinding.set(ModConfig.HANDLER.instance(), "");
                            ModConfig.HANDLER.save();
                            ChatUtils.printChangedSetting(optionBinding.getTranslation(), "");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(setter(unit, optionBinding, greedy ? StringArgumentType.greedyString() : StringArgumentType.string(), StringArgumentType::getString)));

    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> stringList(String command, String unit, ListOptionBinding<String> optionBinding) {
        boolean greedy = true;
        if (optionBinding.getConstraints() instanceof ListConstraints<String> listConstraints && listConstraints.getEntryConstraints() instanceof StringConstraints strConstraints)
            greedy = strConstraints.getSpacesAllowed() == null || strConstraints.getSpacesAllowed();

        return genericList(command, unit, optionBinding, greedy, greedy ? StringArgumentType.greedyString() : StringArgumentType.string(), StringArgumentType::getString);
    }

    public static <T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> enumList(String command, String unit, ListOptionBinding<T> optionBinding) {
        return enumList(command, unit, optionBinding, false);
    }

    public static <T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> enumList(String command, String unit, ListOptionBinding<T> optionBinding, boolean indexed) {
        return genericList(command, unit, optionBinding, indexed, GenericEnumArgumentType.genericEnum(optionBinding.getListType()), (context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, optionBinding.getListType()));
    }



    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> genericList(String command, String unit, ListOptionBinding<T> optionBinding, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return genericList(command, unit, optionBinding, false, argumentType, getArgument);
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> genericList(String command, String unit, ListOptionBinding<T> optionBinding, boolean indexed, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
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
            ArrayList<T> list = new ArrayList<>(optionBinding.get(ModConfig.HANDLER.instance()));

            if (list.size() >= maxSize) {
                ChatUtils.printWithPlaceholders("message.sbutils.listSizeError", maxSize, Component.translatable(optionBinding.getTranslation()));
                return;
            }

            if (!allowDupes && list.contains(value)) {
                ChatUtils.printWithPlaceholders("message.sbutils.listDupeError", value, Component.translatable(optionBinding.getTranslation()));
                return;
            }

            list.add(value);
            optionBinding.set(ModConfig.HANDLER.instance(), list);
            ModConfig.HANDLER.save();
            ChatUtils.printWithPlaceholders("message.sbutils.listAddSuccess", value, Component.translatable(optionBinding.getTranslation()));
            ChatUtils.printListSetting(optionBinding.getTranslation(), list, indexed);
        };

        if (indexed)
            return customIndexedList(command, unit, optionBinding.getPath(), argumentType, getArgument, () -> optionBinding.get(ModConfig.HANDLER.instance()), add,
                    (index) -> {
                        ArrayList<T> list = new ArrayList<>(optionBinding.get(ModConfig.HANDLER.instance()));
                        int adjustedIndex = index - 1;
                        if (adjustedIndex >= list.size() || adjustedIndex < 0) {
                            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable(optionBinding.getTranslation()));
                            return;
                        }

                        T removed = list.remove(adjustedIndex);
                        optionBinding.set(ModConfig.HANDLER.instance(), list);
                        ModConfig.HANDLER.save();
                        ChatUtils.printWithPlaceholders("message.sbutils.listDelSuccess", removed, Component.translatable(optionBinding.getTranslation()));
                        ChatUtils.printListSetting(optionBinding.getTranslation(), list, true);
                    },
                    (index, value) -> {
                        ArrayList<T> list = new ArrayList<>(optionBinding.get(ModConfig.HANDLER.instance()));
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
                        optionBinding.set(ModConfig.HANDLER.instance(), list);
                        ModConfig.HANDLER.save();
                        ChatUtils.printWithPlaceholders("message.sbutils.listAddSuccess", value, Component.translatable(optionBinding.getTranslation()));
                        ChatUtils.printListSetting(optionBinding.getTranslation(), list, true);
                    }
            );

        return customList(command, unit, optionBinding.getPath(), argumentType, getArgument, () -> optionBinding.get(ModConfig.HANDLER.instance()), add,
                (value) -> {
                    ArrayList<T> list = new ArrayList<>(optionBinding.get(ModConfig.HANDLER.instance()));
                    boolean result = list.remove(value);
                    optionBinding.set(ModConfig.HANDLER.instance(), list);
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

    public static <T, S> LiteralArgumentBuilder<FabricClientCommandSource> customList(String command, String unit, String path, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<S>> getPrintable, Consumer<T> add, Consumer<T> del) {
        return runnable(command, () -> ChatUtils.printListSetting(OPTION_KEY + path, getPrintable.get()))
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

    public static <T, S> LiteralArgumentBuilder<FabricClientCommandSource> customIndexedList(String command, String unit, String path, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<S>> getPrintable, Consumer<T> add, Consumer<Integer> del, BiConsumer<Integer, T> insert) {
        return runnable(command, () -> ChatUtils.printListSetting(OPTION_KEY + path, getPrintable.get(), true))
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

    public static LiteralArgumentBuilder<FabricClientCommandSource> bool(String command, OptionBinding<Boolean> optionBinding) {
        return getterSetter(command, "enabled", optionBinding, BoolArgumentType.bool(), BoolArgumentType::getBool);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> doubl(String command, String unit, OptionBinding<Double> optionBinding) {
        return getterSetter(command, unit, optionBinding, getDoubleArgumentType(optionBinding.getConstraints()), DoubleArgumentType::getDouble);
    }

    private static DoubleArgumentType getDoubleArgumentType(Constraints<Double> constraints) {
        if (!(constraints instanceof NumberConstraints<Double> numConstraints))
            return DoubleArgumentType.doubleArg();
        Double min = numConstraints.getMin();
        Double max = numConstraints.getMax();
        return DoubleArgumentType.doubleArg(min == null ? Double.MIN_VALUE : min, max == null ? Double.MAX_VALUE : max);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> integer(String command, String unit, OptionBinding<Integer> optionBinding) {
        return getterSetter(command, unit, optionBinding, IntegerArgumentType.integer(), IntegerArgumentType::getInteger);
    }

    public static <T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> genericEnum(String command, String unit, OptionBinding<T> optionBinding) {
        return getterSetter(command, unit, optionBinding, GenericEnumArgumentType.genericEnum(optionBinding.getType()), ((context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, optionBinding.getType())));
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> getterSetter(String command, String unit, OptionBinding<T> optionBinding, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return getter(command, optionBinding)
                .then(setter(unit, optionBinding, argumentType, getArgument));
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> getter(String command, OptionBinding<T> optionBinding) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    ChatUtils.printSetting(optionBinding.getTranslation(), optionBinding.get(ModConfig.HANDLER.instance()));
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> setter(String argument, OptionBinding<T> optionBinding, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return ClientCommandManager.argument(argument, argumentType)
                .executes(context -> {
                    optionBinding.set(ModConfig.HANDLER.instance(), getArgument.apply(context, argument));
                    ModConfig.HANDLER.save();
                    ChatUtils.printChangedSetting(optionBinding.getTranslation(), optionBinding.get(ModConfig.HANDLER.instance()));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
