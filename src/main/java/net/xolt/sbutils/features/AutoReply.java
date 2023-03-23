package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.LinkedList;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoReply {

    private static long lastMsgSentAt;
    private static LinkedList<String> msgQueue;

    public static void init() {
        msgQueue = new LinkedList<>();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> autoReplyNode = dispatcher.register(ClientCommandManager.literal("autoreply")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoReply = !ModConfig.INSTANCE.getConfig().autoReply;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autoreply", ModConfig.INSTANCE.getConfig().autoReply);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("response")
                        .executes(context ->{
                            Messenger.printSetting("text.sbutils.config.option.autoResponse", ModConfig.INSTANCE.getConfig().autoResponse);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("response", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoResponse = StringArgumentType.getString(context, "response");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoResponse", ModConfig.INSTANCE.getConfig().autoResponse);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoReplyDelay", ModConfig.INSTANCE.getConfig().autoReplyDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoReplyDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoReplyDelay", ModConfig.INSTANCE.getConfig().autoReplyDelay);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("areply")
                .executes(context ->
                        dispatcher.execute("autoreply", context.getSource())
                )
                .redirect(autoReplyNode));
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().autoReply || MC.getNetworkHandler() == null) {
            return;
        }

        if (System.currentTimeMillis() - lastMsgSentAt >= ModConfig.INSTANCE.getConfig().autoReplyDelay * 1000.0) {
            sendMessage();
        }
    }

    public static void processMessage(Text message) {
        if (!ModConfig.INSTANCE.getConfig().autoReply) {
            return;
        }

        Matcher incomingMsg = RegexFilters.incomingMsgFilter.matcher(message.getString());
        if (incomingMsg.matches()) {
            queueResponse(incomingMsg.group(1));
        }
    }

    private static void queueResponse(String sender) {
        msgQueue.offer("msg " + sender + " " + ModConfig.INSTANCE.getConfig().autoResponse);
    }

    private static void sendMessage() {
        if (MC.getNetworkHandler() == null || msgQueue == null || msgQueue.size() == 0) {
            return;
        }

        MC.getNetworkHandler().sendChatCommand(msgQueue.poll());
        lastMsgSentAt = System.currentTimeMillis();
    }
}
