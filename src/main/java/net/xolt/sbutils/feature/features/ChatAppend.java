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

public class ChatAppend extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> addPrefix = new OptionBinding<>("sbutils", "chatAppend.addPrefix", Boolean.class, (config) -> config.chatAppend.addPrefix, (config, value) -> config.chatAppend.addPrefix = value);
    private final OptionBinding<ModConfig, String> prefix = new OptionBinding<>("sbutils", "chatAppend.prefix", String.class, (config) -> config.chatAppend.prefix, (config, value) -> config.chatAppend.prefix = value);
    private final OptionBinding<ModConfig, Boolean> addSuffix = new OptionBinding<>("sbutils", "chatAppend.addSuffix", Boolean.class, (config) -> config.chatAppend.addSuffix, (config, value) -> config.chatAppend.addSuffix = value);
    private final OptionBinding<ModConfig, String> suffix = new OptionBinding<>("sbutils", "chatAppend.suffix", String.class, (config) -> config.chatAppend.suffix, (config, value) -> config.chatAppend.suffix = value);

    public ChatAppend() {
        super("sbutils", "chatAppend", "chatappend", "append");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(addPrefix, prefix, addSuffix, suffix);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> chatAppendNode = dispatcher.register(ClientCommandManager.literal(command)
                .then(CommandHelper.string("prefix", "prefix", prefix, ModConfig.HANDLER)
                        .then(CommandHelper.bool("enabled", addPrefix, ModConfig.HANDLER)))
                .then(CommandHelper.string("suffix", "suffix", suffix, ModConfig.HANDLER)
                        .then(CommandHelper.bool("enabled", addSuffix, ModConfig.HANDLER)))
        );
        registerAlias(dispatcher, chatAppendNode);
    }

    public static ServerboundChatPacket processSentMessage(ServerboundChatPacket packet) {
        if (!ModConfig.instance().chatAppend.addPrefix && !ModConfig.instance().chatAppend.addSuffix)
            return packet;

        String message = packet.message();

        if (ModConfig.instance().chatAppend.addPrefix)
            message = ModConfig.instance().chatAppend.prefix + message;

        if (ModConfig.instance().chatAppend.addSuffix)
            message = message + ModConfig.instance().chatAppend.suffix;

        return new ServerboundChatPacket(message, packet.timeStamp(), packet.salt(), packet.signature(),
                //? if <1.19.4
                //packet.signedPreview(),
                packet.lastSeenMessages());
    }
}
