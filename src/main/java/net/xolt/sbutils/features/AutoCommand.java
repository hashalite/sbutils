package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.gui.KeyValueController.KeyValuePair;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCommand {

    private static final String COMMAND = "autocmd";
    private static final String ALIAS = "acmd";
    private static final HashMap<KeyValuePair<String, KeyValuePair<Double, Boolean>>, Long> cmdsLastSentAt = new HashMap<>();
    private static final Queue<KeyValuePair<String, KeyValuePair<Double, Boolean>>> cmdQueue = new LinkedList<>();

    private static long lastCommandSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoCommandNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoCommand", () -> ModConfig.HANDLER.instance().autoCommand.enabled, (value) -> ModConfig.HANDLER.instance().autoCommand.enabled = value)
                        .then(CommandHelper.doubl("minDelay", "seconds", "autoCommand.minDelay", () -> ModConfig.HANDLER.instance().autoCommand.minDelay, (value) -> ModConfig.HANDLER.instance().autoCommand.minDelay = value))
                        .then(CommandHelper.runnable("commands", AutoCommand::onCommandsCommand)
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
        Messenger.printAutoCommands(ModConfig.HANDLER.instance().autoCommand.commands, cmdsLastSentAt, ModConfig.HANDLER.instance().autoCommand.enabled);
    }

    private static int onAddCommand(double delay, String command) {
        ModConfig.HANDLER.instance().autoCommand.commands.add(new KeyValuePair<>(command, new KeyValuePair<>(delay, false)));
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandAddSuccess", command, delay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onDelCommand(int index) {
        List<KeyValuePair<String, KeyValuePair<Double, Boolean>>> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        KeyValuePair<String, KeyValuePair<Double, Boolean>> command = autoCommands.remove(adjustedIndex);
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandDelSuccess", command.getKey());
        return Command.SINGLE_SUCCESS;
    }

    private static int onToggleCommand(int index) {
        List<KeyValuePair<String, KeyValuePair<Double, Boolean>>> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        KeyValuePair<String, KeyValuePair<Double, Boolean>> command = autoCommands.get(adjustedIndex);
        command.getValue().setValue(!command.getValue().getValue());
        ModConfig.HANDLER.save();
        Messenger.printAutoCommandToggled(command, autoCommands.get(adjustedIndex).getValue().getValue());
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetDelayCommand(int index, double newDelay) {
        List<KeyValuePair<String, KeyValuePair<Double, Boolean>>> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        KeyValuePair<String, KeyValuePair<Double, Boolean>> command = autoCommands.get(adjustedIndex);
        command.getValue().setKey(newDelay);
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.delaySetSuccess", command.getKey(), command.getValue().getKey(), newDelay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetCommandCommand(int index, String newCommand) {
        List<KeyValuePair<String, KeyValuePair<Double, Boolean>>> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        KeyValuePair<String, KeyValuePair<Double, Boolean>> command = autoCommands.get(adjustedIndex);
        command.setKey(newCommand);
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandSetSuccess", command.getKey(), newCommand);
        return Command.SINGLE_SUCCESS;
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoCommand.enabled) {
            return;
        }

        if (System.currentTimeMillis() - lastCommandSentAt < ModConfig.HANDLER.instance().autoCommand.minDelay * 1000) {
            return;
        }

        sendCommands();
    }

    private static void sendCommands() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        for (KeyValuePair<String, KeyValuePair<Double, Boolean>> pair : ModConfig.HANDLER.instance().autoCommand.commands) {
            if (pair.getValue().getValue() && !cmdQueue.contains(pair)) {
                cmdQueue.offer(pair);
            } else if (!pair.getValue().getValue()) {
                cmdQueue.remove(pair);
            }
        }

        if (!cmdQueue.isEmpty()) {
            KeyValuePair<String, KeyValuePair<Double, Boolean>> pair = cmdQueue.poll();
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
