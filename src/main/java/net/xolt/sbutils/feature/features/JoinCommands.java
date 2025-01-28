package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.argument.JoinCommandsEntryArgumentType;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.ModConfig.JoinCommandsConfig.JoinCommandsEntry;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.ChatUtils;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class JoinCommands extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "joinCommands.enabled", Boolean.class, (config) -> config.joinCommands.enabled, (config, value) -> config.joinCommands.enabled = value);
    private final OptionBinding<ModConfig, Double> initialDelay = new OptionBinding<>("sbutils", "joinCommands.initialDelay", Double.class, (config) -> config.joinCommands.initialDelay, (config, value) -> config.joinCommands.initialDelay = value);
    private final OptionBinding<ModConfig, Double> delay = new OptionBinding<>("sbutils", "joinCommands.delay", Double.class, (config) -> config.joinCommands.delay, (config, value) -> config.joinCommands.delay = value);
    private final ListOptionBinding<ModConfig, JoinCommandsEntry> commands = new ListOptionBinding<>("sbutils", "joinCommands.commands", new JoinCommandsEntry("", ""), JoinCommandsEntry.class, (config) -> config.joinCommands.commands, (config, value) -> config.joinCommands.commands = value, new ListConstraints<>(true, null, null));

    private long joinedAt;
    private long lastCommandSentAt;
    private Queue<String> commandQueue;

    public JoinCommands() {
        super("sbutils", "joinCommands", "joincmds", "jc");
        commandQueue = new LinkedList<>();
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, initialDelay, delay, commands);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> joinCommandsNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                    .then(CommandHelper.genericList("commands", "command", commands, ModConfig.HANDLER, true, JoinCommandsEntryArgumentType.commandEntry(), JoinCommandsEntryArgumentType::getCommandEntry)
                            .then(ClientCommandManager.literal("set")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .then(ClientCommandManager.literal("command")
                                                    .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                                            .executes(context -> onSetCommandCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "command")))))
                                            .then(ClientCommandManager.literal("accounts")
                                                    .then(ClientCommandManager.argument("accounts", StringArgumentType.greedyString())
                                                            .executes(context -> onSetAccountsCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "accounts"))))))))
                    .then(CommandHelper.doubl("delay", "seconds", delay, ModConfig.HANDLER))
                    .then(CommandHelper.doubl("initialDelay", "seconds", initialDelay, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, joinCommandsNode);
    }

    private static int onSetCommandCommand(int index, String newCommand) {
        List<ModConfig.JoinCommandsConfig.JoinCommandsEntry> joinCommands = ModConfig.HANDLER.instance().joinCommands.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= joinCommands.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.joinCommands.commands"));
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.JoinCommandsConfig.JoinCommandsEntry command = joinCommands.get(adjustedIndex);
        String oldCommand = command.command;
        command.command = newCommand;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.joinCommands.commandSetSuccess", oldCommand, newCommand);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetAccountsCommand(int index, String newAccounts) {
        List<ModConfig.JoinCommandsConfig.JoinCommandsEntry> joinCommands = ModConfig.HANDLER.instance().joinCommands.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= joinCommands.size() || adjustedIndex < 0) {
            ChatUtils.printWithPlaceholders("message.sbutils.invalidListIndex", index, Component.translatable("text.sbutils.config.option.joinCommands.commands"));
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.JoinCommandsConfig.JoinCommandsEntry command = joinCommands.get(adjustedIndex);
        command.accounts = newAccounts;
        ModConfig.HANDLER.save();
        ChatUtils.printWithPlaceholders("message.sbutils.joinCommands.accountsSetSuccess", command.command, command.formatAccounts());
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().joinCommands.enabled || !SbUtils.SERVER_DETECTOR.isOnSkyblock() || commandQueue.isEmpty())
            return;

        int delay = (int)((joinedAt > lastCommandSentAt ? ModConfig.HANDLER.instance().joinCommands.initialDelay : ModConfig.HANDLER.instance().joinCommands.delay) * 1000.0);

        if (System.currentTimeMillis() - Math.max(joinedAt, lastCommandSentAt) >= delay) {
            sendJoinCommand(commandQueue);
            lastCommandSentAt = System.currentTimeMillis();
        }
    }

    public void onDisconnect() {
        reset();
    }

    public void onJoinGame() {
        if (!ModConfig.HANDLER.instance().joinCommands.enabled || ModConfig.HANDLER.instance().joinCommands.commands.isEmpty() || MC.player == null)
            return;

        reset();
        commandQueue.addAll(getJoinCommands(MC.player));
        joinedAt = System.currentTimeMillis();
    }

    private void reset() {
        joinedAt = 0;
        lastCommandSentAt = 0;
        commandQueue = new LinkedList<>();
    }

    private static List<String> getJoinCommands(Player player) {
        String playerName = player.getName().getString().toLowerCase();
        return ModConfig.HANDLER.instance().joinCommands.commands.stream().filter((command) -> {
            List<String> accounts = command.getAccounts();
            return accounts.isEmpty() || accounts.stream().map(String::toLowerCase).toList().contains(playerName);
        }).map((command) -> command.command).toList();
    }

    private static void sendJoinCommand(Queue<String> commandQueue) {
        if (MC.getConnection() == null)
            return;

        String command = commandQueue.poll();

        if (command == null)
            return;

        if (command.startsWith("/"))
            command = command.substring(1);

        SbUtils.COMMAND_SENDER.sendCommand(command);
    }
}
