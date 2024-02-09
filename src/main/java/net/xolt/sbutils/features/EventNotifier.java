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
                        .then(CommandUtils.bool("title", "eventNotifier.showLlamaTitle", () -> ModConfig.HANDLER.instance().eventNotifier.showLlamaTitle, (value) -> ModConfig.HANDLER.instance().eventNotifier.showLlamaTitle = value))
                        .then(CommandUtils.bool("playSound", "eventNotifier.playLlamaSound", () -> ModConfig.HANDLER.instance().eventNotifier.playLlamaSound, (value) -> ModConfig.HANDLER.instance().eventNotifier.playLlamaSound = value))
                        .then(CommandUtils.genericEnum("sound", "sound", "eventNotifier.llamaSound", ModConfig.NotifSound.class, () -> ModConfig.HANDLER.instance().eventNotifier.llamaSound, (value) -> ModConfig.HANDLER.instance().eventNotifier.llamaSound = value)))
                .then(ClientCommandManager.literal("trader")
                        .then(CommandUtils.bool("title", "eventNotifier.showTraderTitle", () -> ModConfig.HANDLER.instance().eventNotifier.showTraderTitle, (value) -> ModConfig.HANDLER.instance().eventNotifier.showTraderTitle = value))
                        .then(CommandUtils.bool("playSound", "eventNotifier.playTraderSound", () -> ModConfig.HANDLER.instance().eventNotifier.playTraderSound, (value) -> ModConfig.HANDLER.instance().eventNotifier.playTraderSound = value))
                        .then(CommandUtils.genericEnum("sound", "sound", "eventNotifier.traderSound", ModConfig.NotifSound.class, () -> ModConfig.HANDLER.instance().eventNotifier.traderSound, (value) -> ModConfig.HANDLER.instance().eventNotifier.traderSound = value)))
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
        if (ModConfig.HANDLER.instance().eventNotifier.playLlamaSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().eventNotifier.llamaSound.getSound(), 1, 1);
        }

        if (ModConfig.HANDLER.instance().eventNotifier.showLlamaTitle) {
            Messenger.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Formatting.GRAY,"message.sbutils.eventNotifier.vpLlama");
        }
    }

    private static void doTraderNotification() {
        if (ModConfig.HANDLER.instance().eventNotifier.playTraderSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().eventNotifier.traderSound.getSound(), 1, 1);
        }

        if (ModConfig.HANDLER.instance().eventNotifier.showTraderTitle) {
            Messenger.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Formatting.BLUE,"message.sbutils.eventNotifier.wanderingTrader");
        }
    }

    private static boolean enabled() {
        return ModConfig.HANDLER.instance().eventNotifier.showLlamaTitle ||
                ModConfig.HANDLER.instance().eventNotifier.playLlamaSound ||
                ModConfig.HANDLER.instance().eventNotifier.showTraderTitle ||
                ModConfig.HANDLER.instance().eventNotifier.playTraderSound;
    }
}
