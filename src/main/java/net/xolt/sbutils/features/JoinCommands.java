package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class JoinCommands {
    private static boolean waitingToSend;
    private static long joinedAt;
    private static long lastCommandSentAt;
    private static int commandIndex;
    private static List<String> joinCommands;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> joinCommandsNode = dispatcher.register(ClientCommandManager.literal("joincmds")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().joinCmdsEnabled = !ModConfig.INSTANCE.getConfig().joinCmdsEnabled;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.joincommands", ModConfig.INSTANCE.getConfig().joinCmdsEnabled);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("global")
                        .executes(context -> {
                            Messenger.printListSetting("message.sbutils.joinCommands.globalCommandList", getJoinCommands(true));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                        .executes(context ->
                                                onAddCommand(StringArgumentType.getString(context, "command"), true)
                                        )))
                        .then(ClientCommandManager.literal("del")
                                .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                        .executes(context ->
                                                onDelCommand(IntegerArgumentType.getInteger(context, "index"), true)
                                        )))
                        .then(ClientCommandManager.literal("insert")
                                .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                        .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                                .executes(context ->
                                                        onInsertCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "command"), true)
                                                )))))
                .then(ClientCommandManager.literal("account")
                        .executes(context -> {
                            Messenger.printListSetting("message.sbutils.joinCommands.accountCommandList", getJoinCommands(false));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                        .executes(context ->
                                                onAddCommand(StringArgumentType.getString(context, "command"), false)
                                        )))
                        .then(ClientCommandManager.literal("del")
                                .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                        .executes(context ->
                                                onDelCommand(IntegerArgumentType.getInteger(context, "index"), false)
                                        )))
                        .then(ClientCommandManager.literal("insert")
                                .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                        .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                                .executes(context ->
                                                        onInsertCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "command"), false)
                                                )))))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.joinCmdDelay", ModConfig.INSTANCE.getConfig().joinCmdDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("delay", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().joinCmdDelay = DoubleArgumentType.getDouble(context, "delay");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.joinCmdDelay", ModConfig.INSTANCE.getConfig().joinCmdDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("initialDelay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.joinCmdInitialDelay", ModConfig.INSTANCE.getConfig().joinCmdInitialDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("delay", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().joinCmdInitialDelay = DoubleArgumentType.getDouble(context, "delay");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.joinCmdInitialDelay", ModConfig.INSTANCE.getConfig().joinCmdInitialDelay);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("jc")
                .executes(context ->
                        dispatcher.execute("joincmds", context.getSource())
                )
                .redirect(joinCommandsNode));
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().joinCmdsEnabled || !waitingToSend) {
            return;
        }

        int delay = (int)((joinedAt > lastCommandSentAt ? ModConfig.INSTANCE.getConfig().joinCmdInitialDelay : ModConfig.INSTANCE.getConfig().joinCmdDelay) * 1000.0);

        if (System.currentTimeMillis() - Math.max(joinedAt, lastCommandSentAt) >= delay) {
            sendJoinCommand();
        }
    }

    private static int onAddCommand(String command, boolean global) {
        if (MC.player == null) {
            return Command.SINGLE_SUCCESS;
        }

        List<String> joinCommands = getJoinCommands(global);
        joinCommands.add(command);
        IOHandler.writeAccountCommands(MC.player.getGameProfile(), joinCommands);

        Messenger.printListSetting("message.sbutils.joinCommands.addSuccess", joinCommands);

        return Command.SINGLE_SUCCESS;
    }

    private static int onDelCommand(int index, boolean global) {
        if (MC.player == null) {
            return Command.SINGLE_SUCCESS;
        }

        List<String> joinCommands = getJoinCommands(global);

        if ((index - 1) < 0 || (index - 1) >= joinCommands.size()) {
            Messenger.printMessage("message.sbutils.joinCommands.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        joinCommands.remove(index - 1);
        IOHandler.writeAccountCommands(MC.player.getGameProfile(), joinCommands);

        Messenger.printListSetting("message.sbutils.joinCommands.deleteSuccess", joinCommands);

        return Command.SINGLE_SUCCESS;
    }

    private static int onInsertCommand(int index, String command, boolean global) {
        if (MC.player == null) {
            return Command.SINGLE_SUCCESS;
        }

        List<String> joinCommands = getJoinCommands(global);

        if ((index - 1) < 0 || (index - 1) > joinCommands.size()) {
            Messenger.printMessage("message.sbutils.joinCommands.invalidIndex");
            return Command.SINGLE_SUCCESS;
        }

        joinCommands.add(index - 1, command);
        IOHandler.writeAccountCommands(MC.player.getGameProfile(), joinCommands);

        Messenger.printListSetting("message.sbutils.joinCommands.addSuccess", joinCommands);

        return Command.SINGLE_SUCCESS;
    }

    public static void onJoinGame() {
        if (!ModConfig.INSTANCE.getConfig().joinCmdsEnabled) {
            return;
        }

        ClientPlayNetworkHandler networkHandler = MC.getNetworkHandler();
        if (networkHandler == null) {
            return;
        }

        boolean validServer = false;
        for (String server : ModConfig.INSTANCE.getConfig().joinCmdServers) {
            if (networkHandler.getConnection().getAddress().toString().contains(server)) {
                validServer = true;
                break;
            }
        }

        if (!validServer) {
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

        for (int i = 0; i < joinCommands.size(); i++) {
            String command = joinCommands.get(i);
            if (command.length() == 0) {
                joinCommands.remove(i);
            }
        }
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

    public static void reset() {
        waitingToSend = false;
        joinedAt = 0;
        lastCommandSentAt = 0;
        commandIndex = 0;
        joinCommands = null;
    }
}
