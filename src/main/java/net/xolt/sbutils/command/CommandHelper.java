package net.xolt.sbutils.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.xolt.sbutils.command.argument.GenericEnumArgumentType;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandHelper {

    private static final String OPTION_KEY = "text.sbutils.config.option.";
    private static final String CATEGORY_KEY = "text.sbutils.config.category.";

    public static LiteralArgumentBuilder<FabricClientCommandSource> runnable(String command, Runnable runnable) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    runnable.run();
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> toggle(String command, String category, Supplier<Boolean> get, Consumer<Boolean> set) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    set.accept(!get.get());
                    ModConfig.HANDLER.save();
                    ChatUtils.printChangedSetting(CATEGORY_KEY + category, get.get());
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> string(String command, String argument, String setting, Supplier<String> get, Consumer<String> set) {
        return getter(command, setting, get)
                .then(ClientCommandManager.literal("set")
                        .executes(context -> {
                            set.accept("");
                            ModConfig.HANDLER.save();
                            ChatUtils.printChangedSetting(CATEGORY_KEY + setting, "");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(setter(argument, setting, get, set, StringArgumentType.greedyString(), StringArgumentType::getString)));

    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> stringList(String command, String argument, String setting, boolean greedy, Supplier<List<String>> get, Consumer<List<String>> set) {
        return stringList(command, argument, setting, greedy, -1, false, false, get, set);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> stringList(String command, String argument, String setting, boolean greedy, int maxSize, boolean allowDupes, boolean indexed, Supplier<List<String>> get, Consumer<List<String>> set) {
        return genericList(command, argument, setting, maxSize, allowDupes, indexed, greedy ? StringArgumentType.greedyString() : StringArgumentType.string(), StringArgumentType::getString, get, set);
    }

    public static <T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> enumList(String command, String argument, String setting, Class<T> type, Supplier<List<T>> get, Consumer<List<T>> set) {
        return enumList(command, argument, setting, -1, false, false, type, get, set);
    }

    public static <T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> enumList(String command, String argument, String setting, int maxSize, boolean allowDupes, boolean indexed, Class<T> type, Supplier<List<T>> get, Consumer<List<T>> set) {
        return genericList(command, argument, setting, maxSize, allowDupes, indexed, GenericEnumArgumentType.genericEnum(type), (context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, type), get, set);
    }



    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> genericList(String command, String argument, String setting, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<T>> get, Consumer<List<T>> set) {
        return genericList(command, argument, setting, -1, false, false, argumentType, getArgument, get, set);
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> genericList(String command, String argument, String setting, int maxSize, boolean allowDupes, boolean indexed, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<T>> get, Consumer<List<T>> set) {
        Consumer<T> add = (value) -> {
            ArrayList<T> list = new ArrayList<>(get.get());
            if (maxSize > -1 && list.size() >= maxSize) {
                ChatUtils.printWithPlaceholders("message.sbutils.listSizeError", maxSize, Component.translatable(OPTION_KEY + setting));
                return;
            }

            if (!allowDupes && list.contains(value)) {
                ChatUtils.printWithPlaceholders("message.sbutils.listDupeError", value, Component.translatable(OPTION_KEY + setting));
                return;
            }

            list.add(value);
            set.accept(list);
            ModConfig.HANDLER.save();
            ChatUtils.printWithPlaceholders("message.sbutils.listAddSuccess", value, Component.translatable(OPTION_KEY + setting));
            ChatUtils.printListSetting(OPTION_KEY + setting, list, indexed);
        };

        if (indexed)
            return customIndexedList(command, argument, setting, argumentType, getArgument, get, add,
                    (index) -> {
                        ArrayList<T> list = new ArrayList<>(get.get());
                        int adjustedIndex = index - 1;
                        if (adjustedIndex >= list.size() || adjustedIndex < 0) {
                            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable(OPTION_KEY + setting));
                            return;
                        }

                        T removed = list.remove(adjustedIndex);
                        set.accept(list);
                        ModConfig.HANDLER.save();
                        ChatUtils.printWithPlaceholders("message.sbutils.listDelSuccess", removed, Component.translatable(OPTION_KEY + setting));
                        ChatUtils.printListSetting(OPTION_KEY + setting, list, true);
                    },
                    (index, value) -> {
                        ArrayList<T> list = new ArrayList<>(get.get());
                        int adjustedIndex = index - 1;
                        if (adjustedIndex >= list.size() || adjustedIndex < 0) {
                            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable(OPTION_KEY + setting));
                            return;
                        }

                        if (!allowDupes && list.contains(value)) {
                            ChatUtils.printWithPlaceholders("message.sbutils.listDupeError", value, Component.translatable(OPTION_KEY + setting));
                            return;
                        }

                        list.add(index, value);
                        set.accept(list);
                        ModConfig.HANDLER.save();
                        ChatUtils.printWithPlaceholders("message.sbutils.listAddSuccess", value, Component.translatable(OPTION_KEY + setting));
                        ChatUtils.printListSetting(OPTION_KEY + setting, list, true);
                    }
            );

        return customList(command, argument, setting, argumentType, getArgument, get, add,
                (value) -> {
                    ArrayList<T> list = new ArrayList<>(get.get());
                    boolean result = list.remove(value);
                    set.accept(list);
                    ModConfig.HANDLER.save();
                    if (result) {
                        ChatUtils.printWithPlaceholders("message.sbutils.listDelSuccess", value, Component.translatable(OPTION_KEY + setting));
                        ChatUtils.printListSetting(OPTION_KEY + setting, list);
                    } else {
                        ChatUtils.printWithPlaceholders("message.sbutils.listDelFail", value, Component.translatable(OPTION_KEY + setting));
                    }
                }
        );
    }

    public static <T, S> LiteralArgumentBuilder<FabricClientCommandSource> customList(String command, String argument, String setting, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<S>> getPrintable, Consumer<T> add, Consumer<T> del) {
        return runnable(command, () -> ChatUtils.printListSetting(OPTION_KEY + setting, getPrintable.get()))
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument(argument, argumentType)
                                .executes(context -> {
                                    add.accept(getArgument.apply(context, argument));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("del")
                        .then(ClientCommandManager.argument(argument, argumentType)
                                .executes(context -> {
                                    del.accept(getArgument.apply(context, argument));
                                    return Command.SINGLE_SUCCESS;
                                })));
    }

    public static <T, S> LiteralArgumentBuilder<FabricClientCommandSource> customIndexedList(String command, String argument, String setting, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<S>> getPrintable, Consumer<T> add, Consumer<Integer> del, BiConsumer<Integer, T> insert) {
        return runnable(command, () -> ChatUtils.printListSetting(OPTION_KEY + setting, getPrintable.get(), true))
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument(argument, argumentType)
                                .executes(context -> {
                                    add.accept(getArgument.apply(context, argument));
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
                                .then(ClientCommandManager.argument(argument, StringArgumentType.greedyString())
                                        .executes(context -> {
                                            insert.accept(IntegerArgumentType.getInteger(context, "index"), getArgument.apply(context, argument));
                                            return Command.SINGLE_SUCCESS;
                                        }))));
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> bool(String command, String setting, Supplier<Boolean> get, Consumer<Boolean> set) {
        return getterSetter(command, "enabled", setting, get, set, BoolArgumentType.bool(), BoolArgumentType::getBool);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> doubl(String command, String argument, String setting, Supplier<Double> get, Consumer<Double> set) {
        return getterSetter(command, argument, setting, get, set, DoubleArgumentType.doubleArg(), DoubleArgumentType::getDouble);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> doubl(String command, String argument, String setting, Supplier<Double> get, Consumer<Double> set, double min) {
        return getterSetter(command, argument, setting, get, set, DoubleArgumentType.doubleArg(min), DoubleArgumentType::getDouble);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> integer(String command, String argument, String setting, Supplier<Integer> get, Consumer<Integer> set) {
        return getterSetter(command, argument, setting, get, set, IntegerArgumentType.integer(), IntegerArgumentType::getInteger);
    }

    public static <T extends Enum<T> & StringRepresentable> LiteralArgumentBuilder<FabricClientCommandSource> genericEnum(String command, String argument, String setting, Class<T> type, Supplier<T> get, Consumer<T> set) {
        return getterSetter(command, argument, setting, get, set, GenericEnumArgumentType.genericEnum(type), ((context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, type)));
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> getterSetter(String command, String argument, String setting, Supplier<T> get, Consumer<T> set, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return getter(command, setting, get)
                .then(setter(argument, setting, get, set, argumentType, getArgument));
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> getter(String command, String setting, Supplier<T> get) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    ChatUtils.printSetting(OPTION_KEY + setting, get.get());
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> setter(String argument, String setting, Supplier<T> get, Consumer<T> set, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return ClientCommandManager.argument(argument, argumentType)
                .executes(context -> {
                    set.accept(getArgument.apply(context, argument));
                    ModConfig.HANDLER.save();
                    ChatUtils.printChangedSetting(OPTION_KEY + setting, get.get());
                    return Command.SINGLE_SUCCESS;
                });
    }
}
