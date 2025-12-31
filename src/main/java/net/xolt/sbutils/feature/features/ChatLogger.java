package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
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

public class ChatLogger extends Feature<ModConfig> {

    private final OptionBinding<ModConfig, Boolean> shopIncoming = new OptionBinding<>("sbutils", "chatLogger.shopIncoming", Boolean.class, (config) -> config.chatLogger.shopIncoming, (config, value) -> config.chatLogger.shopIncoming = value);
    private final OptionBinding<ModConfig, Boolean> shopOutgoing = new OptionBinding<>("sbutils", "chatLogger.shopOutgoing", Boolean.class, (config) -> config.chatLogger.shopOutgoing, (config, value) -> config.chatLogger.shopOutgoing = value);
    private final OptionBinding<ModConfig, Boolean> msgIncoming = new OptionBinding<>("sbutils", "chatLogger.msgIncoming", Boolean.class, (config) -> config.chatLogger.msgIncoming, (config, value) -> config.chatLogger.msgIncoming = value);
    private final OptionBinding<ModConfig, Boolean> msgOutgoing = new OptionBinding<>("sbutils", "chatLogger.msgOutgoing", Boolean.class, (config) -> config.chatLogger.msgOutgoing, (config, value) -> config.chatLogger.msgOutgoing = value);
    private final OptionBinding<ModConfig, Boolean> visits = new OptionBinding<>("sbutils", "chatLogger.visits", Boolean.class, (config) -> config.chatLogger.visits, (config, value) -> config.chatLogger.visits = value);
    private final OptionBinding<ModConfig, Boolean> dp = new OptionBinding<>("sbutils", "chatLogger.dp", Boolean.class, (config) -> config.chatLogger.dp, (config, value) -> config.chatLogger.dp = value);

    private final List<ChatFilter<ModConfig>> shopFilters = List.of(
            new ChatFilter<>(shopIncoming, ModConfig.HANDLER, List.of(RegexFilters.incomingTransactionFilter)),
            new ChatFilter<>(shopOutgoing, ModConfig.HANDLER, List.of(RegexFilters.outgoingTransactionFilter))
    );
    private final List<ChatFilter<ModConfig>> messageFilters = List.of(
            new ChatFilter<>(msgIncoming, ModConfig.HANDLER, List.of(RegexFilters.incomingMsgFilter)),
            new ChatFilter<>(msgOutgoing, ModConfig.HANDLER, List.of(RegexFilters.outgoingMsgFilter))
    );
    private final List<ChatFilter<ModConfig>> visitFilters = List.of(
            new ChatFilter<>(visits, ModConfig.HANDLER, List.of(RegexFilters.visitFilter))
    );
    private final List<ChatFilter<ModConfig>> dpFilters = List.of(
            new ChatFilter<>(dp, ModConfig.HANDLER, List.of(RegexFilters.dpWinnerFilter))
    );

    public ChatLogger() {
        super("sbutils", "chatLogger", "chatlogger", "logger");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(shopIncoming, shopOutgoing, msgIncoming, msgOutgoing, visits, dp);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatLoggerNode = dispatcher.register(
                CommandHelper.runnable(command, () -> ChatFilters.showEnabledFilters("message.sbutils.chatLogger.status", getFilters()))
                    .then(CommandHelper.bool("incomingShop", shopIncoming, ModConfig.HANDLER))
                    .then(CommandHelper.bool("outgoingShop", shopOutgoing, ModConfig.HANDLER))
                    .then(CommandHelper.bool("incomingMsg", msgIncoming, ModConfig.HANDLER))
                    .then(CommandHelper.bool("outgoingMsg", msgOutgoing, ModConfig.HANDLER))
                    .then(CommandHelper.bool("visit", visits, ModConfig.HANDLER))
                    .then(CommandHelper.bool("dp", dp, ModConfig.HANDLER))
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
                FileUtils.logTransaction(message, messageReceivedAt);

        for (ChatFilter filter : messageFilters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                FileUtils.logMessage(message, messageReceivedAt);

        for (ChatFilter filter : visitFilters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                FileUtils.logVisit(message, messageReceivedAt);

        for (ChatFilter filter : dpFilters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                FileUtils.logDpWinner(message, messageReceivedAt);
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

    public List<ChatFilter<ModConfig>> getFilters() {
        List<ChatFilter<ModConfig>> result = new ArrayList<>();
        result.addAll(shopFilters);
        result.addAll(messageFilters);
        result.addAll(visitFilters);
        result.addAll(dpFilters);
        return result;
    }
}
