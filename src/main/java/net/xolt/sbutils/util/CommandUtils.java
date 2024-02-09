package net.xolt.sbutils.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.StringIdentifiable;
import net.xolt.sbutils.command.argument.GenericEnumArgumentType;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandUtils {

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
                    Messenger.printChangedSetting("text.sbutils.config.category." + category, get.get());
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> string(String command, String argument, String setting, Supplier<String> get, Consumer<String> set) {
        return getter(command, setting, get)
                .then(ClientCommandManager.literal("set")
                        .executes(context -> {
                            set.accept("");
                            ModConfig.HANDLER.save();
                            Messenger.printChangedSetting("text.sbutils.config.option." + setting, "");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(setter(argument, setting, get, set, StringArgumentType.greedyString(), StringArgumentType::getString)));

    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> stringList(String command, String argument, String listMessage, Supplier<List<T>> getPrintable, Consumer<String> add, Consumer<Integer> del, BiConsumer<Integer, String> insert) {
        return list(command, argument, listMessage, StringArgumentType.greedyString(), StringArgumentType::getString, getPrintable, add, del, insert);
    }

    public static <T extends Enum<T> & StringIdentifiable> LiteralArgumentBuilder<FabricClientCommandSource> enumList(String command, String argument, String listMessage, Class<T> type, Supplier<List<T>> getPrintable, Consumer<T> add, Consumer<Integer> del, BiConsumer<Integer, T> insert) {
        return list(command, argument, listMessage, GenericEnumArgumentType.genericEnum(type), (context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, type), getPrintable, add, del, insert);
    }

    public static <T, S> LiteralArgumentBuilder<FabricClientCommandSource> list(String command, String argument, String listMessage, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument, Supplier<List<S>> getPrintable, Consumer<T> add, Consumer<Integer> del, BiConsumer<Integer, T> insert) {
        return runnable(command, () -> Messenger.printListSetting(listMessage, getPrintable.get()))
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

    public static <T extends Enum<T> & StringIdentifiable> LiteralArgumentBuilder<FabricClientCommandSource> genericEnum(String command, String argument, String setting, Class<T> type, Supplier<T> get, Consumer<T> set) {
        return getterSetter(command, argument, setting, get, set, GenericEnumArgumentType.genericEnum(type), ((context, id) -> GenericEnumArgumentType.getGenericEnum(context, id, type)));
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> getterSetter(String command, String argument, String setting, Supplier<T> get, Consumer<T> set, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return getter(command, setting, get)
                .then(setter(argument, setting, get, set, argumentType, getArgument));
    }

    public static <T> LiteralArgumentBuilder<FabricClientCommandSource> getter(String command, String setting, Supplier<T> get) {
        return ClientCommandManager.literal(command)
                .executes(context -> {
                    Messenger.printSetting("text.sbutils.config.option." + setting, get.get());
                    return Command.SINGLE_SUCCESS;
                });
    }

    public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> setter(String argument, String setting, Supplier<T> get, Consumer<T> set, ArgumentType<T> argumentType, BiFunction<CommandContext<FabricClientCommandSource>, String, T> getArgument) {
        return ClientCommandManager.argument(argument, argumentType)
                .executes(context -> {
                    set.accept(getArgument.apply(context, argument));
                    ModConfig.HANDLER.save();
                    Messenger.printChangedSetting("text.sbutils.config.option." + setting, get.get());
                    return Command.SINGLE_SUCCESS;
                });
    }
}
