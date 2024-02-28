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
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.systems.ServerDetector;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.ChatUtils;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class JoinCommands extends Feature {

    private static final String COMMAND = "joincmds";
    private static final String ALIAS = "jc";

    private long joinedAt;
    private long lastCommandSentAt;
    private Queue<String> commandQueue;

    public JoinCommands() {
        commandQueue = new LinkedList<>();
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> joinCommandsNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "joinCommands", () -> ModConfig.HANDLER.instance().joinCommands.enabled, (value) -> ModConfig.HANDLER.instance().joinCommands.enabled = value)
                    .then(CommandHelper.genericList("commands", "command", "joinCommands.commands", -1, true, true, JoinCommandsEntryArgumentType.commandEntry(), JoinCommandsEntryArgumentType::getCommandEntry, () -> ModConfig.HANDLER.instance().joinCommands.commands, (value) -> ModConfig.HANDLER.instance().joinCommands.commands = value)
                            .then(ClientCommandManager.literal("set")
                                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                                            .then(ClientCommandManager.literal("command")
                                                    .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                                            .executes(context -> onSetCommandCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "command")))))
                                            .then(ClientCommandManager.literal("accounts")
                                                    .then(ClientCommandManager.argument("accounts", StringArgumentType.greedyString())
                                                            .executes(context -> onSetAccountsCommand(IntegerArgumentType.getInteger(context, "index"), StringArgumentType.getString(context, "accounts"))))))))
                    .then(CommandHelper.doubl("delay", "seconds", "joinCommands.delay", () -> ModConfig.HANDLER.instance().joinCommands.delay, (value) -> ModConfig.HANDLER.instance().joinCommands.delay = value))
                    .then(CommandHelper.doubl("initialDelay", "seconds", "joinCommands.initialDelay", () -> ModConfig.HANDLER.instance().joinCommands.initialDelay, (value) -> ModConfig.HANDLER.instance().joinCommands.initialDelay = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(joinCommandsNode));
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
        if (!ModConfig.HANDLER.instance().joinCommands.enabled || !SbUtils.SERVER_DETECTOR.isOnSkyblock() || commandQueue.isEmpty()) {
            return;
        }

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
        if (!ModConfig.HANDLER.instance().joinCommands.enabled || ModConfig.HANDLER.instance().joinCommands.commands.isEmpty() || MC.player == null) {
            return;
        }

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

        MC.getConnection().sendCommand(command);
    }
}
