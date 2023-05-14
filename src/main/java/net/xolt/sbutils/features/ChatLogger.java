package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.ChatFilter;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.ArrayList;
import java.util.List;

public class ChatLogger {

    private static final String COMMAND = "chatlogger";
    private static final String ALIAS = "logger";

//    private static List<ChatFilter> shopFilters = List.of(
//            new ChatFilter("text.sbutils.config.option.shopLoggerIncoming",
//                    List.of(RegexFilters.incomingBuyFilter, RegexFilters.incomingSellFilter, RegexFilters.incomingBarterFilter),
//                    () -> ModConfig.INSTANCE.getConfig().shopLoggerIncoming),
//            new ChatFilter("text.sbutils.config.option.shopLoggerOutgoing",
//                    List.of(RegexFilters.outgoingBuyFilter, RegexFilters.outgoingSellFilter, RegexFilters.outgoingBarterFilter),
//                    () -> ModConfig.INSTANCE.getConfig().shopLoggerOutgoing)
//    );
    private static List<ChatFilter> messageFilters = List.of(
            new ChatFilter("text.sbutils.config.option.msgLoggerIncoming", List.of(RegexFilters.incomingMsgFilter), () -> ModConfig.INSTANCE.getConfig().msgLoggerIncoming),
            new ChatFilter("text.sbutils.config.option.msgLoggerOutgoing", List.of(RegexFilters.outgoingMsgFilter), () -> ModConfig.INSTANCE.getConfig().msgLoggerOutgoing)
    );
    private static List<ChatFilter> visitFilters = List.of(
            new ChatFilter("text.sbutils.config.option.visitLogger", List.of(RegexFilters.visitFilter), () -> ModConfig.INSTANCE.getConfig().visitLogger)
    );
    private static List<ChatFilter> dpFilters = List.of(
            new ChatFilter("text.sbutils.config.option.dpLogger", List.of(RegexFilters.dpWinnerFilter), () -> ModConfig.INSTANCE.getConfig().dpLogger)
    );

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> chatLoggerNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context -> {
                    Messenger.printEnabledFilters("message.sbutils.chatLogger.status", getFilters());
                    return Command.SINGLE_SUCCESS;
                })
//                .then(ClientCommandManager.literal("incomingShop")
//                        .executes(context -> {
//                            Messenger.printSetting("text.sbutils.config.option.shopLoggerIncoming", ModConfig.INSTANCE.getConfig().shopLoggerIncoming);
//                            return Command.SINGLE_SUCCESS;
//                        })
//                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
//                                .executes(context -> {
//                                    ModConfig.INSTANCE.getConfig().shopLoggerIncoming = BoolArgumentType.getBool(context, "enabled");
//                                    ModConfig.INSTANCE.save();
//                                    Messenger.printChangedSetting("text.sbutils.config.option.shopLoggerIncoming", ModConfig.INSTANCE.getConfig().shopLoggerIncoming);
//                                    return Command.SINGLE_SUCCESS;
//                                }))
//                .then(ClientCommandManager.literal("outgoingShop")
//                        .executes(context -> {
//                            Messenger.printSetting("text.sbutils.config.option.shopLoggerOutgoing", ModConfig.INSTANCE.getConfig().shopLoggerOutgoing);
//                            return Command.SINGLE_SUCCESS;
//                        })
//                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
//                                .executes(context -> {
//                                    ModConfig.INSTANCE.getConfig().shopLoggerOutgoing = BoolArgumentType.getBool(context, "enabled");
//                                    ModConfig.INSTANCE.save();
//                                    Messenger.printChangedSetting("text.sbutils.config.option.shopLoggerOutgoing", ModConfig.INSTANCE.getConfig().shopLoggerOutgoing);
//                                    return Command.SINGLE_SUCCESS;
//                                }))
                .then(ClientCommandManager.literal("incomingMsg")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.msgLoggerIncoming", ModConfig.INSTANCE.getConfig().msgLoggerIncoming);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().msgLoggerIncoming = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.msgLoggerIncoming", ModConfig.INSTANCE.getConfig().msgLoggerIncoming);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("outgoingMsg")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.msgLoggerOutgoing", ModConfig.INSTANCE.getConfig().msgLoggerOutgoing);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().msgLoggerOutgoing = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.msgLoggerOutgoing", ModConfig.INSTANCE.getConfig().msgLoggerOutgoing);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("visit")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.visitLogger", ModConfig.INSTANCE.getConfig().visitLogger);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().visitLogger = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.visitLogger", ModConfig.INSTANCE.getConfig().visitLogger);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("dp")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.dpLogger", ModConfig.INSTANCE.getConfig().dpLogger);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().dpLogger = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.dpLogger", ModConfig.INSTANCE.getConfig().dpLogger);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute("chatlogger", context.getSource()))
                .redirect(chatLoggerNode));
    }

    public static void processMessage(Text message) {
        if (!anyFiltersEnabled()) {
            return;
        }

        String stringMessage = message.getString();
        long messageReceivedAt = System.currentTimeMillis();

//        for (ChatFilter filter : shopFilters) {
//            if (filter.matches(stringMessage) && filter.isEnabled()) {
//                IOHandler.logTransaction(message, messageReceivedAt);
//            }
//        }

        for (ChatFilter filter : messageFilters) {
            if (filter.matches(stringMessage) && filter.isEnabled()) {
                IOHandler.logMessage(message, messageReceivedAt);
            }
        }

        for (ChatFilter filter : visitFilters) {
            if (filter.matches(stringMessage) && filter.isEnabled()) {
                IOHandler.logVisit(message, messageReceivedAt);
            }
        }

        for (ChatFilter filter : dpFilters) {
            if (filter.matches(stringMessage) && filter.isEnabled()) {
                IOHandler.logDpWinner(message, messageReceivedAt);
            }
        }
    }

    private static boolean anyFiltersEnabled() {
//        for (ChatFilter filter : shopFilters) {
//            if (filter.isEnabled()) {
//                return true;
//            }
//        }

        for (ChatFilter filter : messageFilters) {
            if (filter.isEnabled()) {
                return true;
            }
        }

        for (ChatFilter filter : visitFilters) {
            if (filter.isEnabled()) {
                return true;
            }
        }

        for (ChatFilter filter : dpFilters) {
            if (filter.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public static List<ChatFilter> getFilters() {
        List<ChatFilter> result = new ArrayList<>();
//        result.addAll(shopFilters);
        result.addAll(messageFilters);
        result.addAll(visitFilters);
        result.addAll(dpFilters);
        return result;
    }
}
