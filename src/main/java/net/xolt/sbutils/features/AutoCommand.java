package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.KeyValueController;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCommand {

    private static final String COMMAND = "autocmd";
    private static final String ALIAS = "acmd";
    private static final HashMap<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>, Long> cmdsLastSentAt = new HashMap<>();
    private static final Queue<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>> cmdQueue = new LinkedList<>();

    private static long lastCommandSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoCommandNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autocommand", () -> ModConfig.HANDLER.instance().autoCommandEnabled, (value) -> ModConfig.HANDLER.instance().autoCommandEnabled = value)
                        .then(CommandUtils.doubl("delay", "seconds", "minAutoCommandDelay", () -> ModConfig.HANDLER.instance().minAutoCommandDelay, (value) -> ModConfig.HANDLER.instance().minAutoCommandDelay = value))
                        .then(CommandUtils.runnable("commands", AutoCommand::onCommandsCommand)
                            .then(ClientCommandManager.literal("add")
                                    .then(ClientCommandManager.argument("delay", DoubleArgumentType.doubleArg(1.0))
                                            .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                                    .executes(context -> onAddCommand(DoubleArgumentType.getDouble(context, "delay"), StringArgumentType.getString(context, "command"))))))
                            .then(ClientCommandManager.literal("del")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .executes(context -> onDelCommand(IntegerArgumentType.getInteger(context, "index")))))
                            .then(ClientCommandManager.literal("toggle")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .executes(context -> onToggleCommand(IntegerArgumentType.getInteger(context, "index")))))
                            .then(ClientCommandManager.literal("set")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .then(ClientCommandManager.literal("delay")
                                                    .then(ClientCommandManager.argument("delay", DoubleArgumentType.doubleArg(1.0))
                                                            .executes(context -> onSetDelayCommand(IntegerArgumentType.getInteger(context, "index"), DoubleArgumentType.getDouble(context, "delay")))))
                                            .then(ClientCommandManager.literal("command")
                                                    .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                                            .executes(context -> onSetCommandCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "command")))))
                                    ))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoCommandNode));
    }

    private static void onCommandsCommand() {
        Messenger.printAutoCommands(ModConfig.HANDLER.instance().autoCommands, cmdsLastSentAt, ModConfig.HANDLER.instance().autoCommandEnabled);
    }

    private static int onAddCommand(double delay, String command) {
        List<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>> autoCommands = new ArrayList<>(ModConfig.HANDLER.instance().autoCommands);

        autoCommands.add(new KeyValueController.KeyValuePair<>(command, new KeyValueController.KeyValuePair<>(delay, false)));
        ModConfig.HANDLER.instance().autoCommands = autoCommands;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandAddSuccess", command, delay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onDelCommand(int index) {
        List<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>> autoCommands = new ArrayList<>(ModConfig.HANDLER.instance().autoCommands);

        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }

        KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command = autoCommands.remove(adjustedIndex);
        ModConfig.HANDLER.instance().autoCommands = autoCommands;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandDelSuccess", command.getKey());
        return Command.SINGLE_SUCCESS;
    }

    private static int onToggleCommand(int index) {
        List<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>> autoCommands = new ArrayList<>(ModConfig.HANDLER.instance().autoCommands);
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command = autoCommands.get(adjustedIndex);
        autoCommands.set(adjustedIndex, new KeyValueController.KeyValuePair<>(command.getKey(), new KeyValueController.KeyValuePair<>(command.getValue().getKey(), !command.getValue().getValue())));
        ModConfig.HANDLER.instance().autoCommands = autoCommands;
        ModConfig.HANDLER.save();
        Messenger.printAutoCommandToggled(command, autoCommands.get(adjustedIndex).getValue().getValue());
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetDelayCommand(int index, double newDelay) {
        List<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>> autoCommands = new ArrayList<>(ModConfig.HANDLER.instance().autoCommands);
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command = autoCommands.get(adjustedIndex);
        autoCommands.set(adjustedIndex, new KeyValueController.KeyValuePair<>(command.getKey(), new KeyValueController.KeyValuePair<>(newDelay, command.getValue().getValue())));
        ModConfig.HANDLER.instance().autoCommands = autoCommands;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.delaySetSuccess", command.getKey(), command.getValue().getKey(), newDelay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetCommandCommand(int index, String newCommand) {
        List<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>> autoCommands = new ArrayList<>(ModConfig.HANDLER.instance().autoCommands);
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command = autoCommands.get(adjustedIndex);
        autoCommands.set(adjustedIndex, new KeyValueController.KeyValuePair<>(newCommand, new KeyValueController.KeyValuePair<>(command.getValue().getKey(), command.getValue().getValue())));
        ModConfig.HANDLER.instance().autoCommands = autoCommands;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandSetSuccess", command.getKey(), newCommand);
        return Command.SINGLE_SUCCESS;
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoCommandEnabled) {
            return;
        }

        if (System.currentTimeMillis() - lastCommandSentAt < ModConfig.HANDLER.instance().minAutoCommandDelay * 1000) {
            return;
        }

        sendCommands();
    }

    private static void sendCommands() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        for (KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> pair : ModConfig.HANDLER.instance().autoCommands) {
            if (pair.getValue().getValue() && !cmdQueue.contains(pair)) {
                cmdQueue.offer(pair);
            } else if (!pair.getValue().getValue()) {
                cmdQueue.remove(pair);
            }
        }

        if (!cmdQueue.isEmpty()) {
            KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> pair = cmdQueue.poll();
            if (cmdsLastSentAt.containsKey(pair)) {
                if (System.currentTimeMillis() - cmdsLastSentAt.get(pair) >= pair.getValue().getKey() * 1000) {
                    sendCommand(pair.getKey());
                    long currentTime = System.currentTimeMillis();
                    cmdsLastSentAt.put(pair, currentTime);
                    lastCommandSentAt = currentTime;
                }
            } else {
                sendCommand(pair.getKey());
                long currentTime = System.currentTimeMillis();
                cmdsLastSentAt.put(pair, currentTime);
                lastCommandSentAt = currentTime;
            }
        }
    }

    private static void sendCommand(String command) {
        if (command == null || MC.getNetworkHandler() == null) {
            return;
        }
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        MC.getNetworkHandler().sendChatCommand(command);
    }
}
