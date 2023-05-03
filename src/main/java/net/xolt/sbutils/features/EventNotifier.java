package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import static net.xolt.sbutils.SbUtils.MC;

public class EventNotifier {

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> eventNotifierNode = dispatcher.register(ClientCommandManager.literal("eventnotifier")
                .then(ClientCommandManager.literal("vpLlama")
                        .then(ClientCommandManager.literal("title")
                                .executes(context -> {
                                    Messenger.printSetting("text.sbutils.config.option.showLlamaTitle", ModConfig.INSTANCE.getConfig().showLlamaTitle);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().showLlamaTitle = BoolArgumentType.getBool(context, "enabled");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.showLlamaTitle", ModConfig.INSTANCE.getConfig().showLlamaTitle);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(ClientCommandManager.literal("playSound")
                                .executes(context -> {
                                    Messenger.printSetting("text.sbutils.config.option.playLlamaSound", ModConfig.INSTANCE.getConfig().playLlamaSound);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().playLlamaSound = BoolArgumentType.getBool(context, "enabled");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.playLlamaSound", ModConfig.INSTANCE.getConfig().playLlamaSound);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(ClientCommandManager.literal("sound")
                                .executes(context -> {
                                    Messenger.printSetting("text.sbutils.config.option.llamaSound", ModConfig.INSTANCE.getConfig().llamaSound);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("sound", ModConfig.NotifSound.NotifSoundArgumentType.notifSound())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().llamaSound = ModConfig.NotifSound.NotifSoundArgumentType.getNotifSound(context, "sound");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.llamaSound", ModConfig.INSTANCE.getConfig().llamaSound);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(ClientCommandManager.literal("trader")
                        .then(ClientCommandManager.literal("title")
                                .executes(context -> {
                                    Messenger.printSetting("text.sbutils.config.option.showTraderTitle", ModConfig.INSTANCE.getConfig().showTraderTitle);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().showTraderTitle = BoolArgumentType.getBool(context, "enabled");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.showTraderTitle", ModConfig.INSTANCE.getConfig().showTraderTitle);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(ClientCommandManager.literal("playSound")
                                .executes(context -> {
                                    Messenger.printSetting("text.sbutils.config.option.playTraderSound", ModConfig.INSTANCE.getConfig().playTraderSound);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().playTraderSound = BoolArgumentType.getBool(context, "enabled");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.playTraderSound", ModConfig.INSTANCE.getConfig().playTraderSound);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(ClientCommandManager.literal("sound")
                                .executes(context -> {
                                    Messenger.printSetting("text.sbutils.config.option.traderSound", ModConfig.INSTANCE.getConfig().traderSound);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("sound", ModConfig.NotifSound.NotifSoundArgumentType.notifSound())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().traderSound = ModConfig.NotifSound.NotifSoundArgumentType.getNotifSound(context, "sound");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.traderSound", ModConfig.INSTANCE.getConfig().traderSound);
                                            return Command.SINGLE_SUCCESS;
                                        })))));

        dispatcher.register(ClientCommandManager.literal("enotify")
                .executes(context ->
                        dispatcher.execute("eventnotifier", context.getSource())
                )
                .redirect(eventNotifierNode));
    }

    public static void processMessage(Text message) {
        if (!enabled()) {
            return;
        }

        String stringMessage = message.getString();

        if (RegexFilters.vpLlamaFilter.matcher(stringMessage).matches()) {
            doLlamaNotification();
        } else if (RegexFilters.wanderingTraderFilter.matcher(stringMessage).matches()) {
            doTraderNotification();
        }
    }

    private static void doLlamaNotification() {
        if (ModConfig.INSTANCE.getConfig().playLlamaSound) {
            MC.player.playSound(ModConfig.INSTANCE.getConfig().llamaSound.getSound(), 1, 1);
        }

        if (ModConfig.INSTANCE.getConfig().showLlamaTitle) {
            Messenger.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Formatting.GRAY,"message.sbutils.eventNotifier.vpLlama");
        }
    }

    private static void doTraderNotification() {
        if (ModConfig.INSTANCE.getConfig().playTraderSound) {
            MC.player.playSound(ModConfig.INSTANCE.getConfig().traderSound.getSound(), 1, 1);
        }

        if (ModConfig.INSTANCE.getConfig().showTraderTitle) {
            Messenger.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Formatting.BLUE,"message.sbutils.eventNotifier.wanderingTrader");
        }
    }

    private static boolean enabled() {
        return ModConfig.INSTANCE.getConfig().showLlamaTitle ||
                ModConfig.INSTANCE.getConfig().playLlamaSound ||
                ModConfig.INSTANCE.getConfig().showTraderTitle ||
                ModConfig.INSTANCE.getConfig().playTraderSound;
    }
}
