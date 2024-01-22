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
            new ChatFilter("text.sbutils.config.option.shopLoggerIncoming",
                    List.of(RegexFilters.incomingTransactionFilter),
                    () -> ModConfig.HANDLER.instance().shopLoggerIncoming),
            new ChatFilter("text.sbutils.config.option.shopLoggerOutgoing",
                    List.of(RegexFilters.outgoingTransactionFilter),
                    () -> ModConfig.HANDLER.instance().shopLoggerOutgoing)
    );
    private static List<ChatFilter> messageFilters = List.of(
            new ChatFilter("text.sbutils.config.option.msgLoggerIncoming", List.of(RegexFilters.incomingMsgFilter), () -> ModConfig.HANDLER.instance().msgLoggerIncoming),
            new ChatFilter("text.sbutils.config.option.msgLoggerOutgoing", List.of(RegexFilters.outgoingMsgFilter), () -> ModConfig.HANDLER.instance().msgLoggerOutgoing)
    );
    private static List<ChatFilter> visitFilters = List.of(
            new ChatFilter("text.sbutils.config.option.visitLogger", List.of(RegexFilters.visitFilter), () -> ModConfig.HANDLER.instance().visitLogger)
    );
    private static List<ChatFilter> dpFilters = List.of(
            new ChatFilter("text.sbutils.config.option.dpLogger", List.of(RegexFilters.dpWinnerFilter), () -> ModConfig.HANDLER.instance().dpLogger)
    );

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> chatLoggerNode = dispatcher.register(
                CommandUtils.runnable(COMMAND, () -> Messenger.printEnabledFilters("message.sbutils.chatLogger.status", getFilters()))
                    .then(CommandUtils.bool("incomingShop", "shopLoggerIncoming", () -> ModConfig.HANDLER.instance().shopLoggerIncoming, (value) -> ModConfig.HANDLER.instance().shopLoggerIncoming = value))
                    .then(CommandUtils.bool("outgoingShop", "shopLoggerOutgoing", () -> ModConfig.HANDLER.instance().shopLoggerOutgoing, (value) -> ModConfig.HANDLER.instance().shopLoggerOutgoing = value))
                    .then(CommandUtils.bool("incomingMsg", "msgLoggerIncoming", () -> ModConfig.HANDLER.instance().msgLoggerIncoming, (value) -> ModConfig.HANDLER.instance().msgLoggerIncoming = value))
                    .then(CommandUtils.bool("outgoingMsg", "msgLoggerOutgoing", () -> ModConfig.HANDLER.instance().msgLoggerOutgoing, (value) -> ModConfig.HANDLER.instance().msgLoggerOutgoing = value))
                    .then(CommandUtils.bool("visit", "visitLogger", () -> ModConfig.HANDLER.instance().visitLogger, (value) -> ModConfig.HANDLER.instance().visitLogger = value))
                    .then(CommandUtils.bool("dp", "dpLogger", () -> ModConfig.HANDLER.instance().dpLogger, (value) -> ModConfig.HANDLER.instance().dpLogger = value))
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
