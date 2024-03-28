package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.*;

import java.util.ArrayList;
import java.util.List;

public class ChatLogger {

    private static final String COMMAND = "chatlogger";
    private static final String ALIAS = "logger";

    private static List<ChatFilter> shopFilters = List.of(
            new ChatFilter("text.autoconfig.sbutils.option.chatLogger.shopLoggerIncoming",
                    List.of(RegexFilters.incomingTransactionFilter),
                    () -> ModConfig.INSTANCE.chatLogger.shopLoggerIncoming),
            new ChatFilter("text.autoconfig.sbutils.option.chatLogger.shopLoggerOutgoing",
                    List.of(RegexFilters.outgoingTransactionFilter),
                    () -> ModConfig.INSTANCE.chatLogger.shopLoggerOutgoing)
    );
    private static List<ChatFilter> messageFilters = List.of(
            new ChatFilter("text.autoconfig.sbutils.option.chatLogger.msgLoggerIncoming", List.of(RegexFilters.incomingMsgFilter), () -> ModConfig.INSTANCE.chatLogger.msgLoggerIncoming),
            new ChatFilter("text.autoconfig.sbutils.option.chatLogger.msgLoggerOutgoing", List.of(RegexFilters.outgoingMsgFilter), () -> ModConfig.INSTANCE.chatLogger.msgLoggerOutgoing)
    );
    private static List<ChatFilter> visitFilters = List.of(
            new ChatFilter("text.autoconfig.sbutils.option.chatLogger.visitLogger", List.of(RegexFilters.visitFilter), () -> ModConfig.INSTANCE.chatLogger.visitLogger)
    );
    private static List<ChatFilter> dpFilters = List.of(
            new ChatFilter("text.autoconfig.sbutils.option.chatLogger.dpLogger", List.of(RegexFilters.dpWinnerFilter), () -> ModConfig.INSTANCE.chatLogger.dpLogger)
    );

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> chatLoggerNode = dispatcher.register(
                CommandUtils.runnable(COMMAND, () -> Messenger.printEnabledFilters("message.sbutils.chatLogger.status", getFilters()))
                    .then(CommandUtils.bool("incomingShop", "chatLogger.shopLoggerIncoming", () -> ModConfig.INSTANCE.chatLogger.shopLoggerIncoming, (value) -> ModConfig.INSTANCE.chatLogger.shopLoggerIncoming = value))
                    .then(CommandUtils.bool("outgoingShop", "chatLogger.shopLoggerOutgoing", () -> ModConfig.INSTANCE.chatLogger.shopLoggerOutgoing, (value) -> ModConfig.INSTANCE.chatLogger.shopLoggerOutgoing = value))
                    .then(CommandUtils.bool("incomingMsg", "chatLogger.msgLoggerIncoming", () -> ModConfig.INSTANCE.chatLogger.msgLoggerIncoming, (value) -> ModConfig.INSTANCE.chatLogger.msgLoggerIncoming = value))
                    .then(CommandUtils.bool("outgoingMsg", "chatLogger.msgLoggerOutgoing", () -> ModConfig.INSTANCE.chatLogger.msgLoggerOutgoing, (value) -> ModConfig.INSTANCE.chatLogger.msgLoggerOutgoing = value))
                    .then(CommandUtils.bool("visit", "chatLogger.visitLogger", () -> ModConfig.INSTANCE.chatLogger.visitLogger, (value) -> ModConfig.INSTANCE.chatLogger.visitLogger = value))
                    .then(CommandUtils.bool("dp", "chatLogger.dpLogger", () -> ModConfig.INSTANCE.chatLogger.dpLogger, (value) -> ModConfig.INSTANCE.chatLogger.dpLogger = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource()))
                .redirect(chatLoggerNode));
    }

    public static void processMessage(Text message) {
        if (!anyFiltersEnabled()) {
            return;
        }

        String stringMessage = message.getString();
        long messageReceivedAt = System.currentTimeMillis();

        for (ChatFilter filter : shopFilters) {
            if (filter.matches(stringMessage) && filter.isEnabled()) {
                IOHandler.logTransaction(message, messageReceivedAt);
            }
        }

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
        for (ChatFilter filter : shopFilters) {
            if (filter.isEnabled()) {
                return true;
            }
        }

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
        result.addAll(shopFilters);
        result.addAll(messageFilters);
        result.addAll(visitFilters);
        result.addAll(dpFilters);
        return result;
    }
}
