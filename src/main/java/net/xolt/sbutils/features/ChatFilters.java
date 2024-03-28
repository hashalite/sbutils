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
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.tipsFilterEnabled", List.of(RegexFilters.tipsFilter), () -> ModConfig.INSTANCE.chatFilters.tipsFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.advancementsFilterEnabled", List.of(RegexFilters.advancementsFilter), () -> ModConfig.INSTANCE.chatFilters.advancementsFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.welcomeFilterEnabled", List.of(RegexFilters.welcomeFilter), () -> ModConfig.INSTANCE.chatFilters.welcomeFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.friendJoinFilterEnabled", List.of(RegexFilters.friendJoinFilter), () -> ModConfig.INSTANCE.chatFilters.friendJoinFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.motdFilterEnabled", List.of(RegexFilters.motdFilter), () -> ModConfig.INSTANCE.chatFilters.motdFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.voteFilterEnabled", List.of(RegexFilters.voteFilter), () -> ModConfig.INSTANCE.chatFilters.voteFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.voteRewardFilterEnabled", List.of(RegexFilters.voteRewardFilter), () -> ModConfig.INSTANCE.chatFilters.voteRewardFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.raffleFilterEnabled", List.of(RegexFilters.raffleFilter), () -> ModConfig.INSTANCE.chatFilters.raffleFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.cratesFilterEnabled", List.of(RegexFilters.cratesFilter), () -> ModConfig.INSTANCE.chatFilters.cratesFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.perishedInVoidFilterEnabled", List.of(RegexFilters.perishedInVoidFilter), () -> ModConfig.INSTANCE.chatFilters.perishedInVoidFilterEnabled),
            new ChatFilter("text.autoconfig.sbutils.option.chatFilters.skyChatFilterEnabled", List.of(RegexFilters.skyChatFilter), () -> ModConfig.INSTANCE.chatFilters.skyChatFilterEnabled)
    );

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(
                CommandUtils.runnable(COMMAND, () -> Messenger.printEnabledFilters("message.sbutils.chatFilter.status", filters))
                    .then(CommandUtils.bool("tips", "chatFilters.tipsFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.tipsFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.tipsFilterEnabled = value))
                    .then(CommandUtils.bool("advancements", "chatFilters.advancementsFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.advancementsFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.advancementsFilterEnabled = value))
                    .then(CommandUtils.bool("welcome", "chatFilters.welcomeFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.welcomeFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.welcomeFilterEnabled = value))
                    .then(CommandUtils.bool("friendJoin", "chatFilters.friendJoinFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.friendJoinFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.friendJoinFilterEnabled = value))
                    .then(CommandUtils.bool("motd", "chatFilters.motdFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.motdFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.motdFilterEnabled = value))
                    .then(CommandUtils.bool("vote", "chatFilters.voteFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.voteFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.voteFilterEnabled = value))
                    .then(CommandUtils.bool("voteReward", "chatFilters.voteRewardFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.voteRewardFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.voteRewardFilterEnabled = value))
                    .then(CommandUtils.bool("raffle", "chatFilters.raffleFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.raffleFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.raffleFilterEnabled = value))
                    .then(CommandUtils.bool("crates", "chatFilters.cratesFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.cratesFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.cratesFilterEnabled = value))
                    .then(CommandUtils.bool("perished", "chatFilters.perishedInVoidFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.perishedInVoidFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.perishedInVoidFilterEnabled = value))
                    .then(CommandUtils.bool("skyChat", "chatFilters.skyChatFilterEnabled", () -> ModConfig.INSTANCE.chatFilters.skyChatFilterEnabled, (value) -> ModConfig.INSTANCE.chatFilters.skyChatFilterEnabled = value))
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
