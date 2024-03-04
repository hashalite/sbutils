package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.command.argument.FilterEntryArgumentType;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatFilter;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.sbutils.config.ModConfig.ChatFiltersConfig.FilterEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    private final ListOptionBinding<FilterEntry> customFilters = new ListOptionBinding<>("chatFilters.customFilters", new FilterEntry("", false), FilterEntry.class, (config) -> config.chatFilters.customFilters, (config, value) -> config.chatFilters.customFilters = value);
    private final List<ChatFilter> builtInFilters = List.of(
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
    private final List<Pattern> customRegex;

    public ChatFilters() {
        super("chatFilters", "chatfilter", "filter");
        customFilters.addListener((ignored1, ignored2) -> recompileCustomRegex());
        customRegex = new ArrayList<>();
        recompileCustomRegex();
    }

    @Override public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(tipsFilter, advancementsFilter, welcomeFilter, friendJoinFilter, motdFilter, voteFilter, voteRewardFilter, raffleFilter, cratesFilter, perishedInVoidFilter, skyChatFilter, customFilters);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(
                CommandHelper.runnable(command, () -> ChatUtils.printEnabledFilters("message.sbutils.chatFilter.status", builtInFilters))
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
                    .then(CommandHelper.genericList("custom", "regex", customFilters, true, FilterEntryArgumentType.filterEntry(), FilterEntryArgumentType::getFilterEntry)
                            .then(ClientCommandManager.literal("toggle")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .executes(context -> onToggleCommand(IntegerArgumentType.getInteger(context, "index")))))
                            .then(ClientCommandManager.literal("setRegex")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                                .then(ClientCommandManager.argument("regex", StringArgumentType.greedyString())
                                                        .executes(context -> onSetRegexCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "regex")))))))
        );
        registerAlias(dispatcher, chatFilterNode);
    }

    private int onSetRegexCommand(int index, String newRegex) {
        List<ModConfig.ChatFiltersConfig.FilterEntry> filters = ModConfig.HANDLER.instance().chatFilters.customFilters;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= filters.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.chatFilters.customFilters"));
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.ChatFiltersConfig.FilterEntry filter = filters.get(adjustedIndex);
        String oldRegex = filter.regex;
        filter.regex = newRegex;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.chatFilter.filterSetSuccess", oldRegex, newRegex);
        // This is needed because the listener doesn't pick up on this change
        recompileCustomRegex();
        return Command.SINGLE_SUCCESS;
    }

    private int onToggleCommand(int index) {
        List<ModConfig.ChatFiltersConfig.FilterEntry> filters = ModConfig.HANDLER.instance().chatFilters.customFilters;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= filters.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.chatFilters.customFilters"));
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.ChatFiltersConfig.FilterEntry filter = filters.get(adjustedIndex);
        filter.enabled = !filter.enabled;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.chatFilter.filterToggleSuccess", filter.regex, filter.enabled);
        // This is needed because the listener doesn't pick up on this change
        recompileCustomRegex();
        return Command.SINGLE_SUCCESS;
    }

    public void onChatMessage(Component message, CallbackInfo ci) {
        if (shouldFilter(message, builtInFilters, customRegex)) {
            ci.cancel();
        }
    }

    public void recompileCustomRegex() {
        customRegex.clear();
        for (FilterEntry filter : ModConfig.HANDLER.instance().chatFilters.customFilters)
            if (filter.enabled)
                customRegex.add(Pattern.compile(filter.regex));
    }

    public static boolean shouldFilter(Component message, List<ChatFilter> filters, List<Pattern> customRegex) {
        String stringMessage = message.getString();

        for (ChatFilter filter : filters)
            if (filter.isEnabled() && filter.matches(stringMessage))
                return true;

        for (Pattern pattern : customRegex)
            if (pattern.matcher(stringMessage).matches())
                return true;

        return false;
    }
}
