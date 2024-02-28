package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatFilter;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

public class ChatFilters extends Feature {

    private static final String COMMAND = "chatfilter";
    private static final String ALIAS = "filter";

    private static final List<ChatFilter> filters = List.of(
            new ChatFilter("text.sbutils.config.option.chatFilters.tipsFilter", List.of(RegexFilters.tipsFilter), () -> ModConfig.HANDLER.instance().chatFilters.tipsFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.advancementsFilter", List.of(RegexFilters.advancementsFilter), () -> ModConfig.HANDLER.instance().chatFilters.advancementsFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.welcomeFilter", List.of(RegexFilters.welcomeFilter), () -> ModConfig.HANDLER.instance().chatFilters.welcomeFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.friendJoinFilter", List.of(RegexFilters.friendJoinFilter), () -> ModConfig.HANDLER.instance().chatFilters.friendJoinFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.motdFilter", List.of(RegexFilters.motdFilter), () -> ModConfig.HANDLER.instance().chatFilters.motdFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.voteFilter", List.of(RegexFilters.voteFilter), () -> ModConfig.HANDLER.instance().chatFilters.voteFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.voteRewardFilter", List.of(RegexFilters.voteRewardFilter), () -> ModConfig.HANDLER.instance().chatFilters.voteRewardFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.raffleFilter", List.of(RegexFilters.raffleFilter), () -> ModConfig.HANDLER.instance().chatFilters.raffleFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.cratesFilter", List.of(RegexFilters.cratesFilter), () -> ModConfig.HANDLER.instance().chatFilters.cratesFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.perishedInVoidFilter", List.of(RegexFilters.perishedInVoidFilter), () -> ModConfig.HANDLER.instance().chatFilters.perishedInVoidFilter),
            new ChatFilter("text.sbutils.config.option.chatFilters.skyChatFilter", List.of(RegexFilters.skyChatFilter), () -> ModConfig.HANDLER.instance().chatFilters.skyChatFilter)
    );

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(
                CommandHelper.runnable(COMMAND, () -> ChatUtils.printEnabledFilters("message.sbutils.chatFilter.status", filters))
                    .then(CommandHelper.bool("tips", "chatFilters.tipsFilter", () -> ModConfig.HANDLER.instance().chatFilters.tipsFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.tipsFilter = value))
                    .then(CommandHelper.bool("advancements", "chatFilters.advancementsFilter", () -> ModConfig.HANDLER.instance().chatFilters.advancementsFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.advancementsFilter = value))
                    .then(CommandHelper.bool("welcome", "chatFilters.welcomeFilter", () -> ModConfig.HANDLER.instance().chatFilters.welcomeFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.welcomeFilter = value))
                    .then(CommandHelper.bool("friendJoin", "chatFilters.friendJoinFilter", () -> ModConfig.HANDLER.instance().chatFilters.friendJoinFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.friendJoinFilter = value))
                    .then(CommandHelper.bool("motd", "chatFilters.motdFilter", () -> ModConfig.HANDLER.instance().chatFilters.motdFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.motdFilter = value))
                    .then(CommandHelper.bool("vote", "chatFilters.voteFilter", () -> ModConfig.HANDLER.instance().chatFilters.voteFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.voteFilter = value))
                    .then(CommandHelper.bool("voteReward", "chatFilters.voteRewardFilter", () -> ModConfig.HANDLER.instance().chatFilters.voteRewardFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.voteRewardFilter = value))
                    .then(CommandHelper.bool("raffle", "chatFilters.raffleFilter", () -> ModConfig.HANDLER.instance().chatFilters.raffleFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.raffleFilter = value))
                    .then(CommandHelper.bool("crates", "chatFilters.cratesFilter", () -> ModConfig.HANDLER.instance().chatFilters.cratesFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.cratesFilter = value))
                    .then(CommandHelper.bool("perished", "chatFilters.perishedInVoidFilter", () -> ModConfig.HANDLER.instance().chatFilters.perishedInVoidFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.perishedInVoidFilter = value))
                    .then(CommandHelper.bool("skyChat", "chatFilters.skyChatFilter", () -> ModConfig.HANDLER.instance().chatFilters.skyChatFilter, (value) -> ModConfig.HANDLER.instance().chatFilters.skyChatFilter = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(chatFilterNode));
    }

    public static boolean shouldFilter(Component message) {
        if (!anyFiltersEnabled()) {
            return false;
        }

        String stringMessage = message.getString();

        for (ChatFilter filter : filters) {
            if (filter.matches(stringMessage) && filter.isEnabled()) {
                return true;
            }
        }

        return false;
    }

    private static boolean anyFiltersEnabled() {
        for (ChatFilter filter : filters) {
            if (filter.isEnabled()) {
                return true;
            }
        }
        return false;
    }
}
