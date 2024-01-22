package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class EventNotifier {

    private static final String COMMAND = "eventnotifier";
    private static final String ALIAS = "enotify";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> eventNotifierNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .then(ClientCommandManager.literal("vpLlama")
                        .then(CommandUtils.bool("title", "showLlamaTitle", () -> ModConfig.HANDLER.instance().showLlamaTitle, (value) -> ModConfig.HANDLER.instance().showLlamaTitle = value))
                        .then(CommandUtils.bool("playSound", "playLlamaSound", () -> ModConfig.HANDLER.instance().playLlamaSound, (value) -> ModConfig.HANDLER.instance().playLlamaSound = value))
                        .then(CommandUtils.getterSetter("sound", "sound", "llamaSound", () -> ModConfig.HANDLER.instance().llamaSound, (value) -> ModConfig.HANDLER.instance().llamaSound = value, ModConfig.NotifSound.NotifSoundArgumentType.notifSound(), ModConfig.NotifSound.NotifSoundArgumentType::getNotifSound)))
                .then(ClientCommandManager.literal("trader")
                        .then(CommandUtils.bool("title", "showTraderTitle", () -> ModConfig.HANDLER.instance().showTraderTitle, (value) -> ModConfig.HANDLER.instance().showTraderTitle = value))
                        .then(CommandUtils.bool("playSound", "playTraderSound", () -> ModConfig.HANDLER.instance().playTraderSound, (value) -> ModConfig.HANDLER.instance().playTraderSound = value))
                        .then(CommandUtils.getterSetter("sound", "sound", "traderSound", () -> ModConfig.HANDLER.instance().traderSound, (value) -> ModConfig.HANDLER.instance().traderSound = value, ModConfig.NotifSound.NotifSoundArgumentType.notifSound(), ModConfig.NotifSound.NotifSoundArgumentType::getNotifSound)))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
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
        if (ModConfig.HANDLER.instance().playLlamaSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().llamaSound.getSound(), 1, 1);
        }

        if (ModConfig.HANDLER.instance().showLlamaTitle) {
            Messenger.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Formatting.GRAY,"message.sbutils.eventNotifier.vpLlama");
        }
    }

    private static void doTraderNotification() {
        if (ModConfig.HANDLER.instance().playTraderSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().traderSound.getSound(), 1, 1);
        }

        if (ModConfig.HANDLER.instance().showTraderTitle) {
            Messenger.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Formatting.BLUE,"message.sbutils.eventNotifier.wanderingTrader");
        }
    }

    private static boolean enabled() {
        return ModConfig.HANDLER.instance().showLlamaTitle ||
                ModConfig.HANDLER.instance().playLlamaSound ||
                ModConfig.HANDLER.instance().showTraderTitle ||
                ModConfig.HANDLER.instance().playTraderSound;
    }
}
