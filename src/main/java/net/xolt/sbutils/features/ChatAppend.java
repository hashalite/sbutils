package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;

import java.util.List;

public class ChatAppend {

    private static final String COMMAND = "chatappend";
    private static final String ALIAS = "append";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> chatAppendNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .then(CommandUtils.string("prefix", "prefix", "chatPrefix", () -> ModConfig.HANDLER.instance().chatPrefix, (value) -> ModConfig.HANDLER.instance().chatPrefix = value)
                        .then(CommandUtils.bool("enabled", "addPrefix", () -> ModConfig.HANDLER.instance().addPrefix, (value) -> ModConfig.HANDLER.instance().addPrefix = value)))
                .then(CommandUtils.string("suffix", "suffix", "chatSuffix", () -> ModConfig.HANDLER.instance().chatSuffix, (value) -> ModConfig.HANDLER.instance().chatSuffix = value)
                        .then(CommandUtils.bool("enabled", "addSuffix", () -> ModConfig.HANDLER.instance().addSuffix, (value) -> ModConfig.HANDLER.instance().addSuffix = value)))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(chatAppendNode));
    }

    public static ChatMessageC2SPacket processSentMessage(ChatMessageC2SPacket packet) {
        if (!ModConfig.HANDLER.instance().addPrefix && !ModConfig.HANDLER.instance().addSuffix) {
            return packet;
        }

        String message = packet.chatMessage();

        if (ModConfig.HANDLER.instance().addPrefix) {
            message = ModConfig.HANDLER.instance().chatPrefix + message;
        }

        if (ModConfig.HANDLER.instance().addSuffix) {
            message = message + ModConfig.HANDLER.instance().chatSuffix;
        }

        return new ChatMessageC2SPacket(message, packet.timestamp(), packet.salt(), packet.signature(), packet.acknowledgment());
    }
}
