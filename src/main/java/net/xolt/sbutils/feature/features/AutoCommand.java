package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.command.argument.AutoCommandEntryArgumentType;
import net.xolt.sbutils.config.ModConfig.AutoCommandConfig.AutoCommandEntry;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCommand extends Feature {
    private final OptionBinding<Boolean> enabled = new OptionBinding<>("autoCommand.enabled", Boolean.class, (config) -> config.autoCommand.enabled, (config, value) -> config.autoCommand.enabled = value);
    private final OptionBinding<Double> minDelay = new OptionBinding<>("autoCommand.minDelay", Double.class, (config) -> config.autoCommand.minDelay, (config, value) -> config.autoCommand.minDelay = value);
    private final ListOptionBinding<AutoCommandEntry> commands = new ListOptionBinding<>("autoCommand.commands", new AutoCommandEntry("", 1.0, false), AutoCommandEntry.class, (config) -> config.autoCommand.commands, (config, value) -> config.autoCommand.commands = value, new ListConstraints<>(true, null, null));
    private final Map<AutoCommandEntry, Long> cmdsLastSentAt;
    private final Queue<AutoCommandEntry> cmdQueue;
    private long lastCommandSentAt;

    public AutoCommand() {
        super("autoCommand", "autocmd", "acmd");
        cmdsLastSentAt = new HashMap<>();
        cmdQueue = new LinkedList<>();
    }

    @Override public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, minDelay, commands);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoCommandNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                        .then(CommandHelper.doubl("minDelay", "seconds", minDelay))
                        .then(CommandHelper.genericList("commands", "command", commands, true, AutoCommandEntryArgumentType.commandEntry(), AutoCommandEntryArgumentType::getCommandEntry)
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
        registerAlias(dispatcher, autoCommandNode);
    }

    private static int onToggleCommand(int index) {
        List<AutoCommandEntry> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.autoCommand.commands"));
            return Command.SINGLE_SUCCESS;
        }
        AutoCommandEntry command = autoCommands.get(adjustedIndex);
        command.enabled = !command.enabled;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.autoCommand.commandToggleSuccess", command.command, command.enabled);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetDelayCommand(int index, double newDelay) {
        List<AutoCommandEntry> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.autoCommand.commands"));
            return Command.SINGLE_SUCCESS;
        }
        AutoCommandEntry command = autoCommands.get(adjustedIndex);
        double oldDelay = command.delay;
        command.delay = newDelay;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.autoCommand.delaySetSuccess", command.command, oldDelay, newDelay);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetCommandCommand(int index, String newCommand) {
        List<AutoCommandEntry> autoCommands = ModConfig.HANDLER.instance().autoCommand.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= autoCommands.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.autoCommand.commands"));
            return Command.SINGLE_SUCCESS;
        }
        AutoCommandEntry command = autoCommands.get(adjustedIndex);
        String oldCommand = command.command;
        command.command = newCommand;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.autoCommand.commandSetSuccess", oldCommand, newCommand);
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoCommand.enabled)
            return;

        if (System.currentTimeMillis() - lastCommandSentAt < ModConfig.HANDLER.instance().autoCommand.minDelay * 1000)
            return;

        sendCommands();
    }

    private void sendCommands() {
        if (MC.getConnection() == null)
            return;

        for (AutoCommandEntry command : ModConfig.HANDLER.instance().autoCommand.commands) {
            if (command.enabled && !cmdQueue.contains(command))
                cmdQueue.offer(command);
            else if (!command.enabled)
                cmdQueue.remove(command);
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

    public Long getCmdLastSentAt(AutoCommandEntry command) {
        return cmdsLastSentAt.get(command);
    }

    private static void sendCommand(String command) {
        if (command == null || MC.getConnection() == null)
            return;
        if (command.startsWith("/"))
            command = command.substring(1);
        MC.getConnection().sendCommand(command);
    }
}
