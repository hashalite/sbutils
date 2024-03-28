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
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCommand {

    private static final String COMMAND = "autocmd";
    private static final String ALIAS = "acmd";
    private static final HashMap<ModConfig.AutoCommandConfig.AutoCommandEntry, Long> cmdsLastSentAt = new HashMap<>();
    private static final Queue<ModConfig.AutoCommandConfig.AutoCommandEntry> cmdQueue = new LinkedList<>();

    private static long lastCommandSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoCommandNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoCommand", () -> ModConfig.INSTANCE.autoCommand.autoCommandEnabled, (value) -> ModConfig.INSTANCE.autoCommand.autoCommandEnabled = value)
                        .then(CommandUtils.doubl("delay", "seconds", "autoCommand.minAutoCommandDelay", () -> ModConfig.INSTANCE.autoCommand.minAutoCommandDelay, (value) -> ModConfig.INSTANCE.autoCommand.minAutoCommandDelay = value))
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
        Messenger.printAutoCommands(ModConfig.INSTANCE.autoCommand.autoCommands, cmdsLastSentAt, ModConfig.INSTANCE.autoCommand.autoCommandEnabled);
    }

    private static int onAddCommand(double delay, String command) {
        List<ModConfig.AutoCommandConfig.AutoCommandEntry> autoCommands = new ArrayList<>(ModConfig.INSTANCE.autoCommand.autoCommands);

        autoCommands.add(new ModConfig.AutoCommandConfig.AutoCommandEntry(command, delay, false));
        ModConfig.INSTANCE.autoCommand.autoCommands = autoCommands;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandAddSuccess", command, delay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onDelCommand(int index) {
        List<ModConfig.AutoCommandConfig.AutoCommandEntry> autoCommands = new ArrayList<>(ModConfig.INSTANCE.autoCommand.autoCommands);

        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }

        ModConfig.AutoCommandConfig.AutoCommandEntry command = autoCommands.remove(adjustedIndex);
        ModConfig.INSTANCE.autoCommand.autoCommands = autoCommands;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandDelSuccess", command.command);
        return Command.SINGLE_SUCCESS;
    }

    private static int onToggleCommand(int index) {
        List<ModConfig.AutoCommandConfig.AutoCommandEntry> autoCommands = new ArrayList<>(ModConfig.INSTANCE.autoCommand.autoCommands);
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.AutoCommandConfig.AutoCommandEntry command = autoCommands.get(adjustedIndex);
        autoCommands.set(adjustedIndex, new ModConfig.AutoCommandConfig.AutoCommandEntry(command.command, command.delay, !command.enabled));
        ModConfig.INSTANCE.autoCommand.autoCommands = autoCommands;
        ModConfig.HOLDER.save();
        Messenger.printAutoCommandToggled(command);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetDelayCommand(int index, double newDelay) {
        List<ModConfig.AutoCommandConfig.AutoCommandEntry> autoCommands = new ArrayList<>(ModConfig.INSTANCE.autoCommand.autoCommands);
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.AutoCommandConfig.AutoCommandEntry command = autoCommands.get(adjustedIndex);
        autoCommands.set(adjustedIndex, new ModConfig.AutoCommandConfig.AutoCommandEntry(command.command, newDelay, command.enabled));
        ModConfig.INSTANCE.autoCommand.autoCommands = autoCommands;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.delaySetSuccess", command.command, command.delay, newDelay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetCommandCommand(int index, String newCommand) {
        List<ModConfig.AutoCommandConfig.AutoCommandEntry> autoCommands = new ArrayList<>(ModConfig.INSTANCE.autoCommand.autoCommands);
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoCommand.invalidCommandIndex", index);
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.AutoCommandConfig.AutoCommandEntry command = autoCommands.get(adjustedIndex);
        autoCommands.set(adjustedIndex, new ModConfig.AutoCommandConfig.AutoCommandEntry(newCommand, command.delay, command.enabled));
        ModConfig.INSTANCE.autoCommand.autoCommands = autoCommands;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandSetSuccess", command.command, newCommand);
        return Command.SINGLE_SUCCESS;
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.autoCommand.autoCommandEnabled) {
            return;
        }

        if (System.currentTimeMillis() - lastCommandSentAt < ModConfig.INSTANCE.autoCommand.minAutoCommandDelay * 1000) {
            return;
        }

        sendCommands();
    }

    private static void sendCommands() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        for (ModConfig.AutoCommandConfig.AutoCommandEntry command : ModConfig.INSTANCE.autoCommand.autoCommands) {
            if (command.enabled && !cmdQueue.contains(command)) {
                cmdQueue.offer(command);
            } else if (!command.enabled) {
                cmdQueue.remove(command);
            }
        }

        if (!cmdQueue.isEmpty()) {
            ModConfig.AutoCommandConfig.AutoCommandEntry command = cmdQueue.poll();
            if (cmdsLastSentAt.containsKey(command)) {
                if (System.currentTimeMillis() - cmdsLastSentAt.get(command) >= command.delay * 1000) {
                    sendCommand(command.command);
                    long currentTime = System.currentTimeMillis();
                    cmdsLastSentAt.put(command, currentTime);
                    lastCommandSentAt = currentTime;
                }
            } else {
                sendCommand(command.command);
                long currentTime = System.currentTimeMillis();
                cmdsLastSentAt.put(command, currentTime);
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
