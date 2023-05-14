package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.ChatFilter;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

public class ChatFilters {

    private static final String COMMAND = "chatfilter";
    private static final String ALIAS = "filter";

    private static final List<ChatFilter> filters = List.of(
            new ChatFilter("text.sbutils.config.option.tipsFilterEnabled", List.of(RegexFilters.tipsFilter), () -> ModConfig.INSTANCE.getConfig().tipsFilterEnabled),
            new ChatFilter("text.sbutils.config.option.advancementsFilterEnabled", List.of(RegexFilters.advancementsFilter), () -> ModConfig.INSTANCE.getConfig().advancementsFilterEnabled),
            new ChatFilter("text.sbutils.config.option.welcomeFilterEnabled", List.of(RegexFilters.welcomeFilter), () -> ModConfig.INSTANCE.getConfig().welcomeFilterEnabled),
            new ChatFilter("text.sbutils.config.option.friendJoinFilterEnabled", List.of(RegexFilters.friendJoinFilter), () -> ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled),
            new ChatFilter("text.sbutils.config.option.motdFilterEnabled", List.of(RegexFilters.motdFilter), () -> ModConfig.INSTANCE.getConfig().motdFilterEnabled),
            new ChatFilter("text.sbutils.config.option.voteFilterEnabled", List.of(RegexFilters.voteFilter), () -> ModConfig.INSTANCE.getConfig().voteFilterEnabled),
            new ChatFilter("text.sbutils.config.option.voteRewardFilterEnabled", List.of(RegexFilters.voteRewardFilter), () -> ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled),
            new ChatFilter("text.sbutils.config.option.raffleFilterEnabled", List.of(RegexFilters.raffleFilter), () -> ModConfig.INSTANCE.getConfig().raffleFilterEnabled),
            new ChatFilter("text.sbutils.config.option.cratesFilterEnabled", List.of(RegexFilters.cratesFilter), () -> ModConfig.INSTANCE.getConfig().cratesFilterEnabled),
            new ChatFilter("text.sbutils.config.option.perishedInVoidFilterEnabled", List.of(RegexFilters.perishedInVoidFilter), () -> ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled),
            new ChatFilter("text.sbutils.config.option.skyChatFilterEnabled", List.of(RegexFilters.skyChatFilter), () -> ModConfig.INSTANCE.getConfig().skyChatFilterEnabled)
    );

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                        .executes(context -> {
                            Messenger.printEnabledFilters("message.sbutils.chatFilter.status", filters);
                            return Command.SINGLE_SUCCESS;
                        })
                .then(ClientCommandManager.literal("tips")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.tipsFilterEnabled", ModConfig.INSTANCE.getConfig().tipsFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().tipsFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.tipsFilterEnabled", ModConfig.INSTANCE.getConfig().tipsFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("advancements")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.advancementsFilterEnabled", ModConfig.INSTANCE.getConfig().advancementsFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advancementsFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.advancementsFilterEnabled", ModConfig.INSTANCE.getConfig().advancementsFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("welcome")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.welcomeFilterEnabled", ModConfig.INSTANCE.getConfig().welcomeFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().welcomeFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.welcomeFilterEnabled", ModConfig.INSTANCE.getConfig().welcomeFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("friendJoin")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.friendJoinFilterEnabled", ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.friendJoinFilterEnabled", ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("motd")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.motdFilterEnabled", ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().motdFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.motdFilterEnabled", ModConfig.INSTANCE.getConfig().motdFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("vote")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.voteFilterEnabled", ModConfig.INSTANCE.getConfig().voteFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().voteFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.voteFilterEnabled", ModConfig.INSTANCE.getConfig().voteFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("voteReward")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.voteRewardFilterEnabled", ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.voteRewardFilterEnabled", ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("raffle")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.raffleFilterEnabled", ModConfig.INSTANCE.getConfig().raffleFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().raffleFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.raffleFilterEnabled", ModConfig.INSTANCE.getConfig().raffleFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("crates")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.cratesFilterEnabled", ModConfig.INSTANCE.getConfig().cratesFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().cratesFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.cratesFilterEnabled", ModConfig.INSTANCE.getConfig().cratesFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("perished")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.perishedInVoidFilterEnabled", ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.perishedInVoidFilterEnabled", ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("skyChat")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.skyChatFilterEnabled", ModConfig.INSTANCE.getConfig().skyChatFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().skyChatFilterEnabled = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.skyChatFilterEnabled", ModConfig.INSTANCE.getConfig().skyChatFilterEnabled);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute("chatfilter", context.getSource())
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
