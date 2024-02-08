package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.xolt.sbutils.SbUtils.MC;

public class JoinCommands {

    private static final String COMMAND = "joincmds";
    private static final String ALIAS = "jc";

    private static boolean waitingToSend;
    private static long joinedAt;
    private static long lastCommandSentAt;
    private static int commandIndex;
    private static List<String> joinCommands;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> joinCommandsNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "joinCommands", () -> ModConfig.HANDLER.instance().joinCommands.enabled, (value) -> ModConfig.HANDLER.instance().joinCommands.enabled = value)
                    .then(CommandUtils.stringList("global", "command", "message.sbutils.joinCommands.globalCommandList",
                            () -> getJoinCommands(true),
                            (command) -> onAddCommand(command, true),
                            (index) -> onDelCommand(index, true),
                            (index, command) -> onInsertCommand(index, command, true)))
                    .then(CommandUtils.stringList("account", "command", "message.sbutils.joinCommands.accountCommandList",
                            () -> getJoinCommands(false),
                            (command) -> onAddCommand(command, false),
                            (index) -> onDelCommand(index, false),
                            (index, command) -> onInsertCommand(index, command, false)))
                    .then(CommandUtils.doubl("delay", "seconds", "joinCommands.delay", () -> ModConfig.HANDLER.instance().joinCommands.delay, (value) -> ModConfig.HANDLER.instance().joinCommands.delay = value))
                    .then(CommandUtils.doubl("initialDelay", "seconds", "joinCommands.initialDelay", () -> ModConfig.HANDLER.instance().joinCommands.initialDelay, (value) -> ModConfig.HANDLER.instance().joinCommands.initialDelay = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(joinCommandsNode));
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().joinCommands.enabled || !ServerDetector.isOnSkyblock() || !waitingToSend) {
            return;
        }

        int delay = (int)((joinedAt > lastCommandSentAt ? ModConfig.HANDLER.instance().joinCommands.initialDelay : ModConfig.HANDLER.instance().joinCommands.delay) * 1000.0);

        if (System.currentTimeMillis() - Math.max(joinedAt, lastCommandSentAt) >= delay) {
            sendJoinCommand();
        }
    }

    private static void onAddCommand(String command, boolean global) {
        if (MC.player == null) {
            return;
        }

        List<String> joinCommands = getJoinCommands(global);
        joinCommands.add(command);
        IOHandler.writeAccountCommands(MC.player.getGameProfile(), joinCommands);

        Messenger.printListSetting("message.sbutils.joinCommands.addSuccess", joinCommands);
    }

    private static void onDelCommand(int index, boolean global) {
        if (MC.player == null) {
            return;
        }

        List<String> joinCommands = getJoinCommands(global);

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= joinCommands.size()) {
            Messenger.printMessage("message.sbutils.joinCommands.invalidIndex");
            return;
        }

        joinCommands.remove(adjustedIndex);
        IOHandler.writeAccountCommands(MC.player.getGameProfile(), joinCommands);

        Messenger.printListSetting("message.sbutils.joinCommands.deleteSuccess", joinCommands);
    }

    private static void onInsertCommand(int index, String command, boolean global) {
        if (MC.player == null) {
            return;
        }

        List<String> joinCommands = getJoinCommands(global);

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex > joinCommands.size()) {
            Messenger.printMessage("message.sbutils.joinCommands.invalidIndex");
            return;
        }

        joinCommands.add(adjustedIndex, command);
        IOHandler.writeAccountCommands(MC.player.getGameProfile(), joinCommands);

        Messenger.printListSetting("message.sbutils.joinCommands.addSuccess", joinCommands);
    }

    public static void onDisconnect() {
        reset();
    }

    public static void onJoinGame() {
        if (!ModConfig.HANDLER.instance().joinCommands.enabled) {
            return;
        }

        joinCommands = getJoinCommands();

        if (joinCommands == null || joinCommands.size() == 0) {
            return;
        }

        commandIndex = 0;
        waitingToSend = true;
        joinedAt = System.currentTimeMillis();
    }

    private static List<String> getJoinCommands() {
        if (MC.player == null) {
            return null;
        }

        ArrayList<String> joinCommands = new ArrayList<>();
        joinCommands.addAll(getJoinCommands(true));
        joinCommands.addAll(getJoinCommands(false));

        return joinCommands;
    }

    private static List<String> getJoinCommands(boolean global) {
        if (MC.player == null) {
            return null;
        }

        String fileContents = global ? IOHandler.readGlobalJoinCmds() : IOHandler.readJoinCmdsForAccount(MC.player.getGameProfile());

        List<String> joinCommands = new ArrayList<>(Arrays.asList(fileContents.split("[\\r\\n]+")));

        joinCommands = joinCommands.stream().filter((command) -> command.length() > 0).collect(Collectors.toList());

        return joinCommands;
    }

    private static void sendJoinCommand() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        String command = joinCommands.get(commandIndex);
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        MC.getNetworkHandler().sendChatCommand(command);
        lastCommandSentAt = System.currentTimeMillis();
        commandIndex++;

        if (commandIndex >= joinCommands.size()) {
            reset();
        }
    }

    private static void reset() {
        waitingToSend = false;
        joinedAt = 0;
        lastCommandSentAt = 0;
        commandIndex = 0;
        joinCommands = null;
    }
}
