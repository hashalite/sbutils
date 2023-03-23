package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;

public class ChatAppend {

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> chatAppendNode = dispatcher.register(ClientCommandManager.literal("chatappend")
                .then(ClientCommandManager.literal("prefix")
                        .executes(context -> {
                            Messenger.printChatAppendStatus("text.sbutils.config.option.chatPrefix", ModConfig.INSTANCE.getConfig().addPrefix, ModConfig.INSTANCE.getConfig().chatPrefix);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().addPrefix = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.addPrefix", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().addPrefix = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.addPrefix", false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().chatPrefix = StringArgumentType.getString(context, "prefix");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.chatPrefix", ModConfig.INSTANCE.getConfig().chatPrefix);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(ClientCommandManager.literal("suffix")
                        .executes(context -> {
                            Messenger.printChatAppendStatus("text.sbutils.config.option.chatSuffix", ModConfig.INSTANCE.getConfig().addSuffix, ModConfig.INSTANCE.getConfig().chatSuffix);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().addSuffix = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.addSuffix", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().addSuffix = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.addSuffix", false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("suffix", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().chatSuffix = StringArgumentType.getString(context, "suffix");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.chatSuffix", ModConfig.INSTANCE.getConfig().chatSuffix);
                                            return Command.SINGLE_SUCCESS;
                                        })))));

        dispatcher.register(ClientCommandManager.literal("append")
                .executes(context ->
                        dispatcher.execute("chatappend", context.getSource())
                )
                .redirect(chatAppendNode));
    }

    public static ChatMessageC2SPacket processSentMessage(ChatMessageC2SPacket packet) {
        if (!ModConfig.INSTANCE.getConfig().addPrefix && !ModConfig.INSTANCE.getConfig().addSuffix) {
            return packet;
        }

        String message = packet.chatMessage();

        if (ModConfig.INSTANCE.getConfig().addPrefix) {
            message = ModConfig.INSTANCE.getConfig().chatPrefix + message;
        }

        if (ModConfig.INSTANCE.getConfig().addSuffix) {
            message = message + ModConfig.INSTANCE.getConfig().chatSuffix;
        }

        return new ChatMessageC2SPacket(message, packet.timestamp(), packet.salt(), packet.signature(), packet.acknowledgment());
    }
}
