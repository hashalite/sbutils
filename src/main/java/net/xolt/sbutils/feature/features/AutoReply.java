package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.RegexFilters;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoReply extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "autoReply.enabled", Boolean.class, (config) -> config.autoReply.enabled, (config, value) -> config.autoReply.enabled = value);
    private final OptionBinding<ModConfig, String> response = new OptionBinding<>("sbutils", "autoReply.response", String.class, (config) -> config.autoReply.response, (config, value) -> config.autoReply.response = value);
    private final OptionBinding<ModConfig, Double> delay = new OptionBinding<>("sbutils", "autoReply.delay", Double.class, (config) -> config.autoReply.delay, (config, value) -> config.autoReply.delay = value);

    private long lastMsgSentAt;
    private final LinkedList<String> msgQueue;

    public AutoReply() {
        super("sbutils", "autoReply", "autoreply", "areply");
        msgQueue = new LinkedList<>();
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, response, delay);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoReplyNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                    .then(CommandHelper.string("response", "response", response, ModConfig.HANDLER))
                    .then(CommandHelper.doubl("delay", "seconds", delay, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, autoReplyNode);
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoReply.enabled || MC.getConnection() == null)
            return;

        if (System.currentTimeMillis() - lastMsgSentAt >= ModConfig.HANDLER.instance().autoReply.delay * 1000.0)
            sendMessage();
    }

    public void processMessage(Component message) {
        if (!ModConfig.HANDLER.instance().autoReply.enabled)
            return;

        Matcher incomingMsg = RegexFilters.incomingMsgFilter.matcher(message.getString());
        if (incomingMsg.matches())
            queueResponse(incomingMsg.group(1));
    }

    private void queueResponse(String sender) {
        msgQueue.offer("msg " + sender + " " + ModConfig.HANDLER.instance().autoReply.response);
    }

    private void sendMessage() {
        if (MC.getConnection() == null || msgQueue.isEmpty())
            return;

        SbUtils.COMMAND_SENDER.sendCommand(msgQueue.poll());
        lastMsgSentAt = System.currentTimeMillis();
    }
}
