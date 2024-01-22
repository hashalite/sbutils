package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoReply {

    private static final String COMMAND = "autoreply";
    private static final String ALIAS = "areply";

    private static long lastMsgSentAt;
    private static LinkedList<String> msgQueue;

    public static void init() {
        msgQueue = new LinkedList<>();
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoReplyNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoreply", () -> ModConfig.HANDLER.instance().autoReply, (value) -> ModConfig.HANDLER.instance().autoReply = value)
                    .then(CommandUtils.string("response", "response", "autoResponse", () -> ModConfig.HANDLER.instance().autoResponse, (value) -> ModConfig.HANDLER.instance().autoResponse = value))
                    .then(CommandUtils.doubl("delay", "seconds", "autoReplyDelay", () -> ModConfig.HANDLER.instance().autoReplyDelay, (value) -> ModConfig.HANDLER.instance().autoReplyDelay = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoReplyNode));
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoReply || MC.getNetworkHandler() == null) {
            return;
        }

        if (System.currentTimeMillis() - lastMsgSentAt >= ModConfig.HANDLER.instance().autoReplyDelay * 1000.0) {
            sendMessage();
        }
    }

    public static void processMessage(Text message) {
        if (!ModConfig.HANDLER.instance().autoReply) {
            return;
        }

        Matcher incomingMsg = RegexFilters.incomingMsgFilter.matcher(message.getString());
        if (incomingMsg.matches()) {
            queueResponse(incomingMsg.group(1));
        }
    }

    private static void queueResponse(String sender) {
        msgQueue.offer("msg " + sender + " " + ModConfig.HANDLER.instance().autoResponse);
    }

    private static void sendMessage() {
        if (MC.getNetworkHandler() == null || msgQueue == null || msgQueue.size() == 0) {
            return;
        }

        MC.getNetworkHandler().sendChatCommand(msgQueue.poll());
        lastMsgSentAt = System.currentTimeMillis();
    }
}
