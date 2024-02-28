package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.feature.Feature;

import java.util.List;

public class ChatAppend extends Feature {

    private static final String COMMAND = "chatappend";
    private static final String ALIAS = "append";

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatAppendNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .then(CommandHelper.string("prefix", "prefix", "chatAppend.prefix", () -> ModConfig.HANDLER.instance().chatAppend.prefix, (value) -> ModConfig.HANDLER.instance().chatAppend.prefix = value)
                        .then(CommandHelper.bool("enabled", "chatAppend.addPrefix", () -> ModConfig.HANDLER.instance().chatAppend.addPrefix, (value) -> ModConfig.HANDLER.instance().chatAppend.addPrefix = value)))
                .then(CommandHelper.string("suffix", "suffix", "chatAppend.suffix", () -> ModConfig.HANDLER.instance().chatAppend.suffix, (value) -> ModConfig.HANDLER.instance().chatAppend.suffix = value)
                        .then(CommandHelper.bool("enabled", "chatAppend.addSuffix", () -> ModConfig.HANDLER.instance().chatAppend.addSuffix, (value) -> ModConfig.HANDLER.instance().chatAppend.addSuffix = value)))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(chatAppendNode));
    }

    public static ServerboundChatPacket processSentMessage(ServerboundChatPacket packet) {
        if (!ModConfig.HANDLER.instance().chatAppend.addPrefix && !ModConfig.HANDLER.instance().chatAppend.addSuffix) {
            return packet;
        }

        String message = packet.message();

        if (ModConfig.HANDLER.instance().chatAppend.addPrefix) {
            message = ModConfig.HANDLER.instance().chatAppend.prefix + message;
        }

        if (ModConfig.HANDLER.instance().chatAppend.addSuffix) {
            message = message + ModConfig.HANDLER.instance().chatAppend.suffix;
        }

        return new ServerboundChatPacket(message, packet.timeStamp(), packet.salt(), packet.signature(), packet.lastSeenMessages());
    }
}
