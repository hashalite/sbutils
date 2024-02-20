package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.argument.AutoCommandEntryArgumentType;
import net.xolt.sbutils.config.ModConfig.AutoCommandConfig.AutoCommandEntry;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.Messenger;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCommand {

    private static final String COMMAND = "autocmd";
    private static final String ALIAS = "acmd";
    private static final HashMap<AutoCommandEntry, Long> cmdsLastSentAt = new HashMap<>();
    private static final Queue<AutoCommandEntry> cmdQueue = new LinkedList<>();

    private static long lastCommandSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoCommandNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoCommand", () -> ModConfig.HANDLER.instance().autoCommand.enabled, (value) -> ModConfig.HANDLER.instance().autoCommand.enabled = value)
                        .then(CommandHelper.doubl("minDelay", "seconds", "autoCommand.minDelay", () -> ModConfig.HANDLER.instance().autoCommand.minDelay, (value) -> ModConfig.HANDLER.instance().autoCommand.minDelay = value))
                        .then(CommandHelper.genericList("commands", "command", "autoCommand.commands", -1, true, true, AutoCommandEntryArgumentType.commandEntry(), AutoCommandEntryArgumentType::getCommandEntry, () -> ModConfig.HANDLER.instance().autoCommand.commands, (value) -> ModConfig.HANDLER.instance().autoCommand.commands = value)
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
                                                                        .executes(context -> onSetCommandCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "command")))))))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoCommandNode));
    }

    private static int onToggleCommand(int index) {
        List<AutoCommandEntry> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.invalidListIndex", index, Text.translatable("text.sbutils.config.option.autoCommand.commands"));
            return Command.SINGLE_SUCCESS;
        }
        AutoCommandEntry command = autoCommands.get(adjustedIndex);
        command.enabled = !command.enabled;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandToggleSuccess", command.command, command.enabled);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetDelayCommand(int index, double newDelay) {
        List<AutoCommandEntry> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.invalidListIndex", index, Text.translatable("text.sbutils.config.option.autoCommand.commands"));
            return Command.SINGLE_SUCCESS;
        }
        AutoCommandEntry command = autoCommands.get(adjustedIndex);
        double oldDelay = command.delay;
        command.delay = newDelay;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.delaySetSuccess", command.command, oldDelay, newDelay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetCommandCommand(int index, String newCommand) {
        List<AutoCommandEntry> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.invalidListIndex", index, Text.translatable("text.sbutils.config.option.autoCommand.commands"));
            return Command.SINGLE_SUCCESS;
        }
        AutoCommandEntry command = autoCommands.get(adjustedIndex);
        String oldCommand = command.command;
        command.command = newCommand;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoCommand.commandSetSuccess", oldCommand, newCommand);
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

        for (AutoCommandEntry command : ModConfig.HANDLER.instance().autoCommand.commands) {
            if (command.enabled && !cmdQueue.contains(command)) {
                cmdQueue.offer(command);
            } else if (!command.enabled) {
                cmdQueue.remove(command);
            }
        }

        if (!cmdQueue.isEmpty()) {
            AutoCommandEntry command = cmdQueue.poll();
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

    public static HashMap<AutoCommandEntry, Long> getCmdsLastSentAt() {
        return cmdsLastSentAt;
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
