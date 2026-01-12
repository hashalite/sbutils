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
import net.minecraft.network.chat.MutableComponent;
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

import static net.xolt.sbutils.config.ModConfig.ChatFiltersConfig.CustomFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatFilters extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> tipsFilter = new OptionBinding<>("sbutils", "chatFilters.tipsFilter", Boolean.class, (config) -> config.chatFilters.tipsFilter, (config, value) -> config.chatFilters.tipsFilter = value);
    private final OptionBinding<ModConfig, Boolean> advancementsFilter = new OptionBinding<>("sbutils", "chatFilters.advancementsFilter", Boolean.class, (config) -> config.chatFilters.advancementsFilter, (config, value) -> config.chatFilters.advancementsFilter = value);
    private final OptionBinding<ModConfig, Boolean> welcomeFilter = new OptionBinding<>("sbutils", "chatFilters.welcomeFilter", Boolean.class, (config) -> config.chatFilters.welcomeFilter, (config, value) -> config.chatFilters.welcomeFilter = value);
    private final OptionBinding<ModConfig, Boolean> friendJoinFilter = new OptionBinding<>("sbutils", "chatFilters.friendJoinFilter", Boolean.class, (config) -> config.chatFilters.friendJoinFilter, (config, value) -> config.chatFilters.friendJoinFilter = value);
    private final OptionBinding<ModConfig, Boolean> motdFilter = new OptionBinding<>("sbutils", "chatFilters.motdFilter", Boolean.class, (config) -> config.chatFilters.motdFilter, (config, value) -> config.chatFilters.motdFilter = value);
    private final OptionBinding<ModConfig, Boolean> islandTitleFilter = new OptionBinding<>("sbutils", "chatFilters.islandTitleFilter", Boolean.class, (config) -> config.chatFilters.islandTitleFilter, (config, value) -> config.chatFilters.islandTitleFilter = value);
    private final OptionBinding<ModConfig, Boolean> islandWelcomeFilter = new OptionBinding<>("sbutils", "chatFilters.islandWelcomeFilter", Boolean.class, (config) -> config.chatFilters.islandWelcomeFilter, (config, value) -> config.chatFilters.islandWelcomeFilter = value);
    private final OptionBinding<ModConfig, Boolean> voteFilter = new OptionBinding<>("sbutils", "chatFilters.voteFilter", Boolean.class, (config) -> config.chatFilters.voteFilter, (config, value) -> config.chatFilters.voteFilter = value);
    private final OptionBinding<ModConfig, Boolean> voteRewardFilter = new OptionBinding<>("sbutils", "chatFilters.voteRewardFilter", Boolean.class, (config) -> config.chatFilters.voteRewardFilter, (config, value) -> config.chatFilters.voteRewardFilter = value);
    private final OptionBinding<ModConfig, Boolean> raffleFilter = new OptionBinding<>("sbutils", "chatFilters.raffleFilter", Boolean.class, (config) -> config.chatFilters.raffleFilter, (config, value) -> config.chatFilters.raffleFilter = value);
    private final OptionBinding<ModConfig, Boolean> cratesFilter = new OptionBinding<>("sbutils", "chatFilters.cratesFilter", Boolean.class, (config) -> config.chatFilters.cratesFilter, (config, value) -> config.chatFilters.cratesFilter = value);
    private final OptionBinding<ModConfig, Boolean> perishedInVoidFilter = new OptionBinding<>("sbutils", "chatFilters.perishedInVoidFilter", Boolean.class, (config) -> config.chatFilters.perishedInVoidFilter, (config, value) -> config.chatFilters.perishedInVoidFilter = value);
    private final OptionBinding<ModConfig, Boolean> skyChatFilter = new OptionBinding<>("sbutils", "chatFilters.skyChatFilter", Boolean.class, (config) -> config.chatFilters.skyChatFilter, (config, value) -> config.chatFilters.skyChatFilter = value);
    private final ListOptionBinding<ModConfig, CustomFilter> customFilters = new ListOptionBinding<>("sbutils", "chatFilters.customFilters", new CustomFilter("", ModConfig.FilterTarget.CHAT, false), CustomFilter.class, (config) -> config.chatFilters.customFilters, (config, value) -> config.chatFilters.customFilters = value);
    private final List<ChatFilter<ModConfig>> builtInFilters = List.of(
            new ChatFilter<>(tipsFilter, ModConfig.HANDLER, List.of(RegexFilters.tipsFilter)),
            new ChatFilter<>(advancementsFilter, ModConfig.HANDLER, List.of(RegexFilters.advancementsFilter)),
            new ChatFilter<>(welcomeFilter, ModConfig.HANDLER, List.of(RegexFilters.welcomeFilter)),
            new ChatFilter<>(friendJoinFilter, ModConfig.HANDLER, List.of(RegexFilters.friendJoinFilter)),
            new ChatFilter<>(motdFilter, ModConfig.HANDLER, List.of(RegexFilters.motdFilter)),
            new ChatFilter<>(islandTitleFilter, ModConfig.HANDLER, List.of(RegexFilters.islandTitleFilter), true),
            new ChatFilter<>(islandWelcomeFilter, ModConfig.HANDLER, List.of(RegexFilters.islandWelcomeFilter)),
            new ChatFilter<>(voteFilter, ModConfig.HANDLER, List.of(RegexFilters.voteFilter)),
            new ChatFilter<>(voteRewardFilter, ModConfig.HANDLER, List.of(RegexFilters.voteRewardFilter)),
            new ChatFilter<>(raffleFilter, ModConfig.HANDLER, List.of(RegexFilters.raffleFilter)),
            new ChatFilter<>(cratesFilter, ModConfig.HANDLER, List.of(RegexFilters.cratesFilter)),
            new ChatFilter<>(perishedInVoidFilter, ModConfig.HANDLER, List.of(RegexFilters.perishedInVoidFilter)),
            new ChatFilter<>(skyChatFilter, ModConfig.HANDLER, List.of(RegexFilters.skyChatFilter))
    );
    private final List<Pattern> chatCustomRegex;
    private final List<Pattern> titleCustomRegex;

    public ChatFilters() {
        super("sbutils", "chatFilters", "chatfilter", "filter");
        customFilters.addListener((ignored1, ignored2) -> recompileCustomRegex());
        chatCustomRegex = new ArrayList<>();
        titleCustomRegex = new ArrayList<>();
        recompileCustomRegex();
    }

    @Override public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(tipsFilter, advancementsFilter, welcomeFilter, friendJoinFilter, motdFilter, islandTitleFilter, islandWelcomeFilter, voteFilter, voteRewardFilter, raffleFilter, cratesFilter, perishedInVoidFilter, skyChatFilter, customFilters);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(
                CommandHelper.runnable(command, () -> showEnabledFilters("message.sbutils.chatFilter.status", builtInFilters))
                    .then(CommandHelper.bool("tips", tipsFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("advancements", advancementsFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("welcome", welcomeFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("friendJoin", friendJoinFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("motd", motdFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("vote", voteFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("voteReward", voteRewardFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("raffle", raffleFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("crates", cratesFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("perished", perishedInVoidFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("skyChat", skyChatFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("islandTitle", islandTitleFilter, ModConfig.HANDLER))
                    .then(CommandHelper.bool("islandWelcome", islandWelcomeFilter, ModConfig.HANDLER))
                    .then(CommandHelper.genericList("custom", "regex", customFilters, ModConfig.HANDLER, true, FilterEntryArgumentType.filterEntry(), FilterEntryArgumentType::getFilterEntry)
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
        List<CustomFilter> filters = ModConfig.instance().chatFilters.customFilters;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= filters.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.chatFilters.customFilters"));
            return Command.SINGLE_SUCCESS;
        }
        CustomFilter filter = filters.get(adjustedIndex);
        String oldRegex = filter.regex;
        filter.regex = newRegex;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.chatFilter.filterSetSuccess", oldRegex, newRegex);
        // This is needed because the listener doesn't pick up on this change
        recompileCustomRegex();
        return Command.SINGLE_SUCCESS;
    }

    private int onToggleCommand(int index) {
        List<CustomFilter> filters = ModConfig.instance().chatFilters.customFilters;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= filters.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.chatFilters.customFilters"));
            return Command.SINGLE_SUCCESS;
        }
        CustomFilter filter = filters.get(adjustedIndex);
        filter.enabled = !filter.enabled;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.chatFilter.filterToggleSuccess", filter.regex, filter.enabled);
        // This is needed because the listener doesn't pick up on this change
        recompileCustomRegex();
        return Command.SINGLE_SUCCESS;
    }

    public void onTitle(Component message, CallbackInfo ci) {
        if (shouldFilterTitle(message))
            ci.cancel();
    }

    public void onChatMessage(Component message, CallbackInfo ci) {
        if (shouldFilterChat(message))
            ci.cancel();
    }

    public void recompileCustomRegex() {
        chatCustomRegex.clear();
        titleCustomRegex.clear();
        for (CustomFilter filter : ModConfig.instance().chatFilters.customFilters) {
            if (!filter.enabled)
                continue;

            Pattern pattern = Pattern.compile(filter.regex);

            switch (filter.target) {
                case CHAT -> chatCustomRegex.add(pattern);
                case TITLE -> titleCustomRegex.add(pattern);
            }
        }
    }

    public boolean shouldFilterTitle(Component message) {
        return shouldFilter(message, true);
    }

    public boolean shouldFilterChat(Component message) {
        return shouldFilter(message, false);
    }

    public boolean shouldFilter(Component message, boolean titles) {
        String stringMessage = message.getString();

        for (ChatFilter<ModConfig> filter : builtInFilters)
            if (filter.isEnabled() && filter.titles() == titles && filter.matches(stringMessage))
                return true;

        for (Pattern pattern : titles ? titleCustomRegex : chatCustomRegex)
            if (pattern.matcher(stringMessage).matches())
                return true;

        return false;
    }

    public static void showEnabledFilters(String message, List<ChatFilter<ModConfig>> filters) {
        ChatUtils.printMessage(Component.translatable(message));
        List<MutableComponent> formatted = filters.stream().map(ChatFilter::format).toList();
        ChatUtils.printList(formatted, false);
    }
}
