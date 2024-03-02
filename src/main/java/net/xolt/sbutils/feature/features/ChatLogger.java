package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.*;

import java.util.ArrayList;
import java.util.List;

public class ChatLogger extends Feature {

    private final OptionBinding<Boolean> shopIncoming = new OptionBinding<>("chatLogger.shopIncoming", Boolean.class, (config) -> config.chatLogger.shopIncoming, (config, value) -> config.chatLogger.shopIncoming = value);
    private final OptionBinding<Boolean> shopOutgoing = new OptionBinding<>("chatLogger.shopOutgoing", Boolean.class, (config) -> config.chatLogger.shopOutgoing, (config, value) -> config.chatLogger.shopOutgoing = value);
    private final OptionBinding<Boolean> msgIncoming = new OptionBinding<>("chatLogger.msgIncoming", Boolean.class, (config) -> config.chatLogger.msgIncoming, (config, value) -> config.chatLogger.msgIncoming = value);
    private final OptionBinding<Boolean> msgOutgoing = new OptionBinding<>("chatLogger.msgOutgoing", Boolean.class, (config) -> config.chatLogger.msgOutgoing, (config, value) -> config.chatLogger.msgOutgoing = value);
    private final OptionBinding<Boolean> visits = new OptionBinding<>("chatLogger.visits", Boolean.class, (config) -> config.chatLogger.visits, (config, value) -> config.chatLogger.visits = value);
    private final OptionBinding<Boolean> dp = new OptionBinding<>("chatLogger.dp", Boolean.class, (config) -> config.chatLogger.dp, (config, value) -> config.chatLogger.dp = value);

    private final List<ChatFilter> shopFilters = List.of(
            new ChatFilter(shopIncoming, List.of(RegexFilters.incomingTransactionFilter)),
            new ChatFilter(shopOutgoing, List.of(RegexFilters.outgoingTransactionFilter))
    );
    private final List<ChatFilter> messageFilters = List.of(
            new ChatFilter(msgIncoming, List.of(RegexFilters.incomingMsgFilter)),
            new ChatFilter(msgOutgoing, List.of(RegexFilters.outgoingMsgFilter))
    );
    private final List<ChatFilter> visitFilters = List.of(
            new ChatFilter(visits, List.of(RegexFilters.visitFilter))
    );
    private final List<ChatFilter> dpFilters = List.of(
            new ChatFilter(dp, List.of(RegexFilters.dpWinnerFilter))
    );

    public ChatLogger() {
        super("chatLogger", "chatlogger", "logger");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(shopIncoming, shopOutgoing, msgIncoming, msgOutgoing, visits, dp);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatLoggerNode = dispatcher.register(
                CommandHelper.runnable(command, () -> ChatUtils.printEnabledFilters("message.sbutils.chatLogger.status", getFilters()))
                    .then(CommandHelper.bool("incomingShop", shopIncoming))
                    .then(CommandHelper.bool("outgoingShop", shopOutgoing))
                    .then(CommandHelper.bool("incomingMsg", msgIncoming))
                    .then(CommandHelper.bool("outgoingMsg", msgOutgoing))
                    .then(CommandHelper.bool("visit", visits))
                    .then(CommandHelper.bool("dp", dp))
        );
        registerAlias(dispatcher, chatLoggerNode);
    }

    public void processMessage(Component message) {
        if (!anyFiltersEnabled())
            return;

        String stringMessage = message.getString();
        long messageReceivedAt = System.currentTimeMillis();

        for (ChatFilter filter : shopFilters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                IOHandler.logTransaction(message, messageReceivedAt);

        for (ChatFilter filter : messageFilters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                IOHandler.logMessage(message, messageReceivedAt);

        for (ChatFilter filter : visitFilters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                IOHandler.logVisit(message, messageReceivedAt);

        for (ChatFilter filter : dpFilters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                IOHandler.logDpWinner(message, messageReceivedAt);
    }

    private boolean anyFiltersEnabled() {
        for (ChatFilter filter : shopFilters)
            if (filter.isEnabled())
                return true;

        for (ChatFilter filter : messageFilters)
            if (filter.isEnabled())
                return true;

        for (ChatFilter filter : visitFilters)
            if (filter.isEnabled())
                return true;

        for (ChatFilter filter : dpFilters)
            if (filter.isEnabled())
                return true;
        return false;
    }

    public List<ChatFilter> getFilters() {
        List<ChatFilter> result = new ArrayList<>();
        result.addAll(shopFilters);
        result.addAll(messageFilters);
        result.addAll(visitFilters);
        result.addAll(dpFilters);
        return result;
    }
}
