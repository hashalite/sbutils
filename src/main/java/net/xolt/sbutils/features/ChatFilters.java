package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.ChatFilter;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

public class ChatFilters {

    private static final List<ChatFilter> filters = List.of(
            new ChatFilter("text.sbutils.config.option.tipsFilterEnabled", List.of(RegexFilters.tipsFilter), () -> ModConfig.INSTANCE.getConfig().tipsFilterEnabled),
            new ChatFilter("text.sbutils.config.option.advancementsFilterEnabled", List.of(RegexFilters.advancementsFilter), () -> ModConfig.INSTANCE.getConfig().advancementsFilterEnabled),
            new ChatFilter("text.sbutils.config.option.welcomeFilterEnabled", List.of(RegexFilters.welcomeFilter), () -> ModConfig.INSTANCE.getConfig().welcomeFilterEnabled),
            new ChatFilter("text.sbutils.config.option.friendJoinFilterEnabled", List.of(RegexFilters.friendJoinFilter), () -> ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled),
            new ChatFilter("text.sbutils.config.option.motdFilterEnabled", List.of(RegexFilters.motdFilter), () -> ModConfig.INSTANCE.getConfig().motdFilterEnabled),
            new ChatFilter("text.sbutils.config.option.voteFilterEnabled", List.of(RegexFilters.voteFilter), () -> ModConfig.INSTANCE.getConfig().voteFilterEnabled),
            new ChatFilter("text.sbutils.config.option.voteRewardFilterEnabled", List.of(RegexFilters.voteRewardFilter), () -> ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled),
            new ChatFilter("text.sbutils.config.option.lotteryFilterEnabled", List.of(RegexFilters.lotteryFilter), () -> ModConfig.INSTANCE.getConfig().lotteryFilterEnabled),
            new ChatFilter("text.sbutils.config.option.cratesFilterEnabled", List.of(RegexFilters.cratesFilter), () -> ModConfig.INSTANCE.getConfig().cratesFilterEnabled),
            new ChatFilter("text.sbutils.config.option.clearLagFilterEnabled", List.of(RegexFilters.clearLagFilter), () -> ModConfig.INSTANCE.getConfig().clearLagFilterEnabled),
            new ChatFilter("text.sbutils.config.option.perishedInVoidFilterEnabled", List.of(RegexFilters.perishedInVoidFilter), () -> ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled),
            new ChatFilter("text.sbutils.config.option.skyChatFilterEnabled", List.of(RegexFilters.skyChatFilter), () -> ModConfig.INSTANCE.getConfig().skyChatFilterEnabled)
    );

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> chatFilterNode = dispatcher.register(ClientCommandManager.literal("chatfilter")
                        .executes(context -> {
                            Messenger.printEnabledFilters("message.sbutils.chatFilter.status", filters);
                            return Command.SINGLE_SUCCESS;
                        })
                .then(ClientCommandManager.literal("tips")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.tipsFilterEnabled", ModConfig.INSTANCE.getConfig().tipsFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().tipsFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.tipsFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().tipsFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.tipsFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("advancements")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.advancementsFilterEnabled", ModConfig.INSTANCE.getConfig().advancementsFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advancementsFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.advancementsFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().advancementsFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.advancementsFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("welcome")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.welcomeFilterEnabled", ModConfig.INSTANCE.getConfig().welcomeFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().welcomeFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.welcomeFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().welcomeFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.welcomeFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("friendJoin")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.friendJoinFilterEnabled", ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.friendJoinFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.friendJoinFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("motd")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.motdFilterEnabled", ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.motdFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().friendJoinFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.motdFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("vote")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.voteFilterEnabled", ModConfig.INSTANCE.getConfig().voteFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().voteFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.voteFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().voteFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.voteFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("voteReward")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.voteRewardFilterEnabled", ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.voteRewardFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().voteRewardFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.voteRewardFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("lottery")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.lotteryFilterEnabled", ModConfig.INSTANCE.getConfig().lotteryFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().lotteryFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.lotteryFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().lotteryFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.lotteryFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("crates")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.cratesFilterEnabled", ModConfig.INSTANCE.getConfig().cratesFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().cratesFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.cratesFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().cratesFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.cratesFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("clearLag")
                        .executes(context -> {
                            Messenger.printSetting( "text.sbutils.config.option.clearLagFilterEnabled", ModConfig.INSTANCE.getConfig().clearLagFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().clearLagFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting( "text.sbutils.config.option.clearLagFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().clearLagFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting( "text.sbutils.config.option.clearLagFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("perished")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.perishedInVoidFilterEnabled", ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.perishedInVoidFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().perishedInVoidFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.perishedInVoidFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("skyChat")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.skyChatFilterEnabled", ModConfig.INSTANCE.getConfig().skyChatFilterEnabled);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().skyChatFilterEnabled = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.skyChatFilterEnabled", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().skyChatFilterEnabled = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.skyChatFilterEnabled", false);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("filter")
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
