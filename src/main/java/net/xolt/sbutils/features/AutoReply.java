package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
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
                CommandHelper.toggle(COMMAND, "autoReply", () -> ModConfig.HANDLER.instance().autoReply.enabled, (value) -> ModConfig.HANDLER.instance().autoReply.enabled = value)
                    .then(CommandHelper.string("response", "response", "autoReply.response", () -> ModConfig.HANDLER.instance().autoReply.response, (value) -> ModConfig.HANDLER.instance().autoReply.response = value))
                    .then(CommandHelper.doubl("delay", "seconds", "autoReply.delay", () -> ModConfig.HANDLER.instance().autoReply.delay, (value) -> ModConfig.HANDLER.instance().autoReply.delay = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoReplyNode));
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoReply.enabled || MC.getNetworkHandler() == null) {
            return;
        }

        if (System.currentTimeMillis() - lastMsgSentAt >= ModConfig.HANDLER.instance().autoReply.delay * 1000.0) {
            sendMessage();
        }
    }

    public static void processMessage(Text message) {
        if (!ModConfig.HANDLER.instance().autoReply.enabled) {
            return;
        }

        Matcher incomingMsg = RegexFilters.incomingMsgFilter.matcher(message.getString());
        if (incomingMsg.matches()) {
            queueResponse(incomingMsg.group(1));
        }
    }

    private static void queueResponse(String sender) {
        msgQueue.offer("msg " + sender + " " + ModConfig.HANDLER.instance().autoReply.response);
    }

    private static void sendMessage() {
        if (MC.getNetworkHandler() == null || msgQueue == null || msgQueue.size() == 0) {
            return;
        }

        MC.getNetworkHandler().sendChatCommand(msgQueue.poll());
        lastMsgSentAt = System.currentTimeMillis();
    }
}
