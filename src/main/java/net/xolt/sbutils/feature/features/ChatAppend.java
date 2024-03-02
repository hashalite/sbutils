package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;

import java.util.List;

public class ChatAppend extends Feature {
    private final OptionBinding<Boolean> addPrefix = new OptionBinding<>("chatAppend.addPrefix", Boolean.class, (config) -> config.chatAppend.addPrefix, (config, value) -> config.chatAppend.addPrefix = value);
    private final OptionBinding<String> prefix = new OptionBinding<>("chatAppend.prefix", String.class, (config) -> config.chatAppend.prefix, (config, value) -> config.chatAppend.prefix = value);
    private final OptionBinding<Boolean> addSuffix = new OptionBinding<>("chatAppend.addSuffix", Boolean.class, (config) -> config.chatAppend.addSuffix, (config, value) -> config.chatAppend.addSuffix = value);
    private final OptionBinding<String> suffix = new OptionBinding<>("chatAppend.suffix", String.class, (config) -> config.chatAppend.suffix, (config, value) -> config.chatAppend.suffix = value);

    public ChatAppend() {
        super("chatAppend", "chatappend", "append");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(addPrefix, prefix, addSuffix, suffix);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatAppendNode = dispatcher.register(ClientCommandManager.literal(command)
                .then(CommandHelper.string("prefix", "prefix", prefix)
                        .then(CommandHelper.bool("enabled", addPrefix)))
                .then(CommandHelper.string("suffix", "suffix", suffix)
                        .then(CommandHelper.bool("enabled", addSuffix)))
        );
        registerAlias(dispatcher, chatAppendNode);
    }

    public static ServerboundChatPacket processSentMessage(ServerboundChatPacket packet) {
        if (!ModConfig.HANDLER.instance().chatAppend.addPrefix && !ModConfig.HANDLER.instance().chatAppend.addSuffix)
            return packet;

        String message = packet.message();

        if (ModConfig.HANDLER.instance().chatAppend.addPrefix)
            message = ModConfig.HANDLER.instance().chatAppend.prefix + message;

        if (ModConfig.HANDLER.instance().chatAppend.addSuffix)
            message = message + ModConfig.HANDLER.instance().chatAppend.suffix;

        return new ServerboundChatPacket(message, packet.timeStamp(), packet.salt(), packet.signature(), packet.lastSeenMessages());
    }
}
