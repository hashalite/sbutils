package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.ChatFilter;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

public class ChatFilters {

    private static final String COMMAND = "chatfilter";
    private static final String ALIAS = "filter";

    private static final List<ChatFilter> filters = List.of(
            new ChatFilter("text.sbutils.config.option.tipsFilterEnabled", List.of(RegexFilters.tipsFilter), () -> ModConfig.HANDLER.instance().tipsFilterEnabled),
            new ChatFilter("text.sbutils.config.option.advancementsFilterEnabled", List.of(RegexFilters.advancementsFilter), () -> ModConfig.HANDLER.instance().advancementsFilterEnabled),
            new ChatFilter("text.sbutils.config.option.welcomeFilterEnabled", List.of(RegexFilters.welcomeFilter), () -> ModConfig.HANDLER.instance().welcomeFilterEnabled),
            new ChatFilter("text.sbutils.config.option.friendJoinFilterEnabled", List.of(RegexFilters.friendJoinFilter), () -> ModConfig.HANDLER.instance().friendJoinFilterEnabled),
            new ChatFilter("text.sbutils.config.option.motdFilterEnabled", List.of(RegexFilters.motdFilter), () -> ModConfig.HANDLER.instance().motdFilterEnabled),
            new ChatFilter("text.sbutils.config.option.voteFilterEnabled", List.of(RegexFilters.voteFilter), () -> ModConfig.HANDLER.instance().voteFilterEnabled),
            new ChatFilter("text.sbutils.config.option.voteRewardFilterEnabled", List.of(RegexFilters.voteRewardFilter), () -> ModConfig.HANDLER.instance().voteRewardFilterEnabled),
            new ChatFilter("text.sbutils.config.option.raffleFilterEnabled", List.of(RegexFilters.raffleFilter), () -> ModConfig.HANDLER.instance().raffleFilterEnabled),
            new ChatFilter("text.sbutils.config.option.cratesFilterEnabled", List.of(RegexFilters.cratesFilter), () -> ModConfig.HANDLER.instance().cratesFilterEnabled),
            new ChatFilter("text.sbutils.config.option.perishedInVoidFilterEnabled", List.of(RegexFilters.perishedInVoidFilter), () -> ModConfig.HANDLER.instance().perishedInVoidFilterEnabled),
            new ChatFilter("text.sbutils.config.option.skyChatFilterEnabled", List.of(RegexFilters.skyChatFilter), () -> ModConfig.HANDLER.instance().skyChatFilterEnabled)
    );

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(
                CommandUtils.runnable(COMMAND, () -> Messenger.printEnabledFilters("message.sbutils.chatFilter.status", filters))
                    .then(CommandUtils.bool("tips", "tipsFilterEnabled", () -> ModConfig.HANDLER.instance().tipsFilterEnabled, (value) -> ModConfig.HANDLER.instance().tipsFilterEnabled = value))
                    .then(CommandUtils.bool("advancements", "advancementsFilterEnabled", () -> ModConfig.HANDLER.instance().advancementsFilterEnabled, (value) -> ModConfig.HANDLER.instance().advancementsFilterEnabled = value))
                    .then(CommandUtils.bool("welcome", "welcomeFilterEnabled", () -> ModConfig.HANDLER.instance().welcomeFilterEnabled, (value) -> ModConfig.HANDLER.instance().welcomeFilterEnabled = value))
                    .then(CommandUtils.bool("friendJoin", "friendJoinFilterEnabled", () -> ModConfig.HANDLER.instance().friendJoinFilterEnabled, (value) -> ModConfig.HANDLER.instance().friendJoinFilterEnabled = value))
                    .then(CommandUtils.bool("motd", "motdFilterEnabled", () -> ModConfig.HANDLER.instance().motdFilterEnabled, (value) -> ModConfig.HANDLER.instance().motdFilterEnabled = value))
                    .then(CommandUtils.bool("vote", "voteFilterEnabled", () -> ModConfig.HANDLER.instance().voteFilterEnabled, (value) -> ModConfig.HANDLER.instance().voteFilterEnabled = value))
                    .then(CommandUtils.bool("voteReward", "voteRewardFilterEnabled", () -> ModConfig.HANDLER.instance().voteRewardFilterEnabled, (value) -> ModConfig.HANDLER.instance().voteRewardFilterEnabled = value))
                    .then(CommandUtils.bool("raffle", "raffleFilterEnabled", () -> ModConfig.HANDLER.instance().raffleFilterEnabled, (value) -> ModConfig.HANDLER.instance().raffleFilterEnabled = value))
                    .then(CommandUtils.bool("crates", "cratesFilterEnabled", () -> ModConfig.HANDLER.instance().cratesFilterEnabled, (value) -> ModConfig.HANDLER.instance().cratesFilterEnabled = value))
                    .then(CommandUtils.bool("perished", "perishedInVoidFilterEnabled", () -> ModConfig.HANDLER.instance().perishedInVoidFilterEnabled, (value) -> ModConfig.HANDLER.instance().perishedInVoidFilterEnabled = value))
                    .then(CommandUtils.bool("skyChat", "skyChatFilterEnabled", () -> ModConfig.HANDLER.instance().skyChatFilterEnabled, (value) -> ModConfig.HANDLER.instance().skyChatFilterEnabled = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(chatFilterNode));
    }

    public static boolean shouldFilter(Text message) {
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
