package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatFilter;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public class ChatFilters extends Feature {
    private final OptionBinding<Boolean> tipsFilter = new OptionBinding<>("chatFilters.tipsFilter", Boolean.class, (config) -> config.chatFilters.tipsFilter, (config, value) -> config.chatFilters.tipsFilter = value);
    private final OptionBinding<Boolean> advancementsFilter = new OptionBinding<>("chatFilters.advancementsFilter", Boolean.class, (config) -> config.chatFilters.advancementsFilter, (config, value) -> config.chatFilters.advancementsFilter = value);
    private final OptionBinding<Boolean> welcomeFilter = new OptionBinding<>("chatFilters.welcomeFilter", Boolean.class, (config) -> config.chatFilters.welcomeFilter, (config, value) -> config.chatFilters.welcomeFilter = value);
    private final OptionBinding<Boolean> friendJoinFilter = new OptionBinding<>("chatFilters.friendJoinFilter", Boolean.class, (config) -> config.chatFilters.friendJoinFilter, (config, value) -> config.chatFilters.friendJoinFilter = value);
    private final OptionBinding<Boolean> motdFilter = new OptionBinding<>("chatFilters.motdFilter", Boolean.class, (config) -> config.chatFilters.motdFilter, (config, value) -> config.chatFilters.motdFilter = value);
    private final OptionBinding<Boolean> voteFilter = new OptionBinding<>("chatFilters.voteFilter", Boolean.class, (config) -> config.chatFilters.voteFilter, (config, value) -> config.chatFilters.voteFilter = value);
    private final OptionBinding<Boolean> voteRewardFilter = new OptionBinding<>("chatFilters.voteRewardFilter", Boolean.class, (config) -> config.chatFilters.voteRewardFilter, (config, value) -> config.chatFilters.voteRewardFilter = value);
    private final OptionBinding<Boolean> raffleFilter = new OptionBinding<>("chatFilters.raffleFilter", Boolean.class, (config) -> config.chatFilters.raffleFilter, (config, value) -> config.chatFilters.raffleFilter = value);
    private final OptionBinding<Boolean> cratesFilter = new OptionBinding<>("chatFilters.cratesFilter", Boolean.class, (config) -> config.chatFilters.cratesFilter, (config, value) -> config.chatFilters.cratesFilter = value);
    private final OptionBinding<Boolean> perishedInVoidFilter = new OptionBinding<>("chatFilters.perishedInVoidFilter", Boolean.class, (config) -> config.chatFilters.perishedInVoidFilter, (config, value) -> config.chatFilters.perishedInVoidFilter = value);
    private final OptionBinding<Boolean> skyChatFilter = new OptionBinding<>("chatFilters.skyChatFilter", Boolean.class, (config) -> config.chatFilters.skyChatFilter, (config, value) -> config.chatFilters.skyChatFilter = value);
    private final List<ChatFilter> filters = List.of(
            new ChatFilter(tipsFilter, List.of(RegexFilters.tipsFilter)),
            new ChatFilter(advancementsFilter, List.of(RegexFilters.advancementsFilter)),
            new ChatFilter(welcomeFilter, List.of(RegexFilters.welcomeFilter)),
            new ChatFilter(friendJoinFilter, List.of(RegexFilters.friendJoinFilter)),
            new ChatFilter(motdFilter, List.of(RegexFilters.motdFilter)),
            new ChatFilter(voteFilter, List.of(RegexFilters.voteFilter)),
            new ChatFilter(voteRewardFilter, List.of(RegexFilters.voteRewardFilter)),
            new ChatFilter(raffleFilter, List.of(RegexFilters.raffleFilter)),
            new ChatFilter(cratesFilter, List.of(RegexFilters.cratesFilter)),
            new ChatFilter(perishedInVoidFilter, List.of(RegexFilters.perishedInVoidFilter)),
            new ChatFilter(skyChatFilter, List.of(RegexFilters.skyChatFilter))
    );

    public ChatFilters() {
        super("chatFilters", "chatfilter", "filter");
    }

    @Override public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(tipsFilter, advancementsFilter, welcomeFilter, friendJoinFilter, motdFilter, voteFilter, voteRewardFilter, raffleFilter, cratesFilter, perishedInVoidFilter, skyChatFilter);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(
                CommandHelper.runnable(command, () -> ChatUtils.printEnabledFilters("message.sbutils.chatFilter.status", filters))
                    .then(CommandHelper.bool("tips", tipsFilter))
                    .then(CommandHelper.bool("advancements", advancementsFilter))
                    .then(CommandHelper.bool("welcome", welcomeFilter))
                    .then(CommandHelper.bool("friendJoin", friendJoinFilter))
                    .then(CommandHelper.bool("motd", motdFilter))
                    .then(CommandHelper.bool("vote", voteFilter))
                    .then(CommandHelper.bool("voteReward", voteRewardFilter))
                    .then(CommandHelper.bool("raffle", raffleFilter))
                    .then(CommandHelper.bool("crates", cratesFilter))
                    .then(CommandHelper.bool("perished", perishedInVoidFilter))
                    .then(CommandHelper.bool("skyChat", skyChatFilter))
        );
        registerAlias(dispatcher, chatFilterNode);
    }

    public void onChatMessage(Component message, CallbackInfo ci) {
        if (shouldFilter(message, filters)) {
            ci.cancel();
        }
    }

    public static boolean shouldFilter(Component message, List<ChatFilter> filters) {
        if (!anyFiltersEnabled(filters))
            return false;

        String stringMessage = message.getString();

        for (ChatFilter filter : filters)
            if (filter.matches(stringMessage) && filter.isEnabled())
                return true;
        return false;
    }

    private static boolean anyFiltersEnabled(List<ChatFilter> filters) {
        for (ChatFilter filter : filters)
            if (filter.isEnabled())
                return true;
        return false;
    }
}
