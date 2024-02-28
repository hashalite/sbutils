package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.*;

import java.util.ArrayList;
import java.util.List;

public class ChatLogger extends Feature {

    private static final String COMMAND = "chatlogger";
    private static final String ALIAS = "logger";

    private static final List<ChatFilter> shopFilters = List.of(
            new ChatFilter("text.sbutils.config.option.chatLogger.shopIncoming",
                    List.of(RegexFilters.incomingTransactionFilter),
                    () -> ModConfig.HANDLER.instance().chatLogger.shopIncoming),
            new ChatFilter("text.sbutils.config.option.chatLogger.shopOutgoing",
                    List.of(RegexFilters.outgoingTransactionFilter),
                    () -> ModConfig.HANDLER.instance().chatLogger.shopOutgoing)
    );
    private static final List<ChatFilter> messageFilters = List.of(
            new ChatFilter("text.sbutils.config.option.chatLogger.msgIncoming",
                    List.of(RegexFilters.incomingMsgFilter),
                    () -> ModConfig.HANDLER.instance().chatLogger.msgIncoming),
            new ChatFilter("text.sbutils.config.option.chatLogger.msgOutgoing",
                    List.of(RegexFilters.outgoingMsgFilter),
                    () -> ModConfig.HANDLER.instance().chatLogger.msgOutgoing)
    );
    private static final List<ChatFilter> visitFilters = List.of(
            new ChatFilter("text.sbutils.config.option.chatLogger.visits",
                    List.of(RegexFilters.visitFilter),
                    () -> ModConfig.HANDLER.instance().chatLogger.visits)
    );
    private static final List<ChatFilter> dpFilters = List.of(
            new ChatFilter("text.sbutils.config.option.chatLogger.dp",
                    List.of(RegexFilters.dpWinnerFilter),
                    () -> ModConfig.HANDLER.instance().chatLogger.dp)
    );

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatLoggerNode = dispatcher.register(
                CommandHelper.runnable(COMMAND, () -> ChatUtils.printEnabledFilters("message.sbutils.chatLogger.status", getFilters()))
                    .then(CommandHelper.bool("incomingShop", "chatLogger.shopIncoming", () -> ModConfig.HANDLER.instance().chatLogger.shopIncoming, (value) -> ModConfig.HANDLER.instance().chatLogger.shopIncoming = value))
                    .then(CommandHelper.bool("outgoingShop", "chatLogger.shopOutgoing", () -> ModConfig.HANDLER.instance().chatLogger.shopOutgoing, (value) -> ModConfig.HANDLER.instance().chatLogger.shopOutgoing = value))
                    .then(CommandHelper.bool("incomingMsg", "chatLogger.msgIncoming", () -> ModConfig.HANDLER.instance().chatLogger.msgIncoming, (value) -> ModConfig.HANDLER.instance().chatLogger.msgIncoming = value))
                    .then(CommandHelper.bool("outgoingMsg", "chatLogger.msgOutgoing", () -> ModConfig.HANDLER.instance().chatLogger.msgOutgoing, (value) -> ModConfig.HANDLER.instance().chatLogger.msgOutgoing = value))
                    .then(CommandHelper.bool("visit", "chatLogger.visits", () -> ModConfig.HANDLER.instance().chatLogger.visits, (value) -> ModConfig.HANDLER.instance().chatLogger.visits = value))
                    .then(CommandHelper.bool("dp", "chatLogger.dp", () -> ModConfig.HANDLER.instance().chatLogger.dp, (value) -> ModConfig.HANDLER.instance().chatLogger.dp = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource()))
                .redirect(chatLoggerNode));
    }

    public static void processMessage(Component message) {
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
