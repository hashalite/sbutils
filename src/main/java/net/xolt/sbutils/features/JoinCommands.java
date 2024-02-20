package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.argument.JoinCommandsEntryArgumentType;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

import java.util.*;
import java.util.stream.Collectors;

import static net.xolt.sbutils.SbUtils.MC;

public class JoinCommands {

    private static final String COMMAND = "joincmds";
    private static final String ALIAS = "jc";

    private static long joinedAt;
    private static long lastCommandSentAt;
    private static Queue<String> commandQueue = new LinkedList<>();

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> joinCommandsNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "joinCommands", () -> ModConfig.HANDLER.instance().joinCommands.enabled, (value) -> ModConfig.HANDLER.instance().joinCommands.enabled = value)
                    .then(CommandHelper.genericList("commands", "command", "joinCommands.commands", -1, true, true, JoinCommandsEntryArgumentType.commandEntry(), JoinCommandsEntryArgumentType::getCommandEntry, () -> ModConfig.HANDLER.instance().joinCommands.commands)
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
            Messenger.printWithPlaceholders("message.sbutils.invalidListIndex", index, Text.translatable("text.sbutils.config.option.joinCommands.commands"));
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.JoinCommandsConfig.JoinCommandsEntry command = joinCommands.get(adjustedIndex);
        String oldCommand = command.command;
        command.command = newCommand;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.joinCommands.commandSetSuccess", oldCommand, newCommand);
        return Command.SINGLE_SUCCESS;
    }

    private static int onSetAccountsCommand(int index, String newAccounts) {
        List<ModConfig.JoinCommandsConfig.JoinCommandsEntry> joinCommands = ModConfig.HANDLER.instance().joinCommands.commands;
        int adjustedIndex = index - 1;
        if (adjustedIndex >= joinCommands.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.invalidListIndex", index, Text.translatable("text.sbutils.config.option.joinCommands.commands"));
            return Command.SINGLE_SUCCESS;
        }
        ModConfig.JoinCommandsConfig.JoinCommandsEntry command = joinCommands.get(adjustedIndex);
        command.accounts = newAccounts;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.joinCommands.accountsSetSuccess", command.command, command.formatAccounts());
        return Command.SINGLE_SUCCESS;
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().joinCommands.enabled || !ServerDetector.isOnSkyblock() || commandQueue.isEmpty()) {
            return;
        }

        int delay = (int)((joinedAt > lastCommandSentAt ? ModConfig.HANDLER.instance().joinCommands.initialDelay : ModConfig.HANDLER.instance().joinCommands.delay) * 1000.0);

        if (System.currentTimeMillis() - Math.max(joinedAt, lastCommandSentAt) >= delay) {
            sendJoinCommand();
        }
    }

    public static void onDisconnect() {
        reset();
    }

    public static void onJoinGame() {
        if (!ModConfig.HANDLER.instance().joinCommands.enabled || ModConfig.HANDLER.instance().joinCommands.commands.isEmpty() || MC.player == null) {
            return;
        }

        reset();
        commandQueue.addAll(getJoinCommands(MC.player));
        joinedAt = System.currentTimeMillis();
    }

    private static List<String> getJoinCommands(PlayerEntity player) {
        String playerName = player.getName().getString().toLowerCase();
        return ModConfig.HANDLER.instance().joinCommands.commands.stream().filter((command) -> {
            List<String> accounts = command.getAccounts();
            return accounts.isEmpty() || accounts.stream().map(String::toLowerCase).toList().contains(playerName);
        }).map((command) -> command.command).toList();
    }

    private static void sendJoinCommand() {
        if (MC.getNetworkHandler() == null)
            return;

        String command = commandQueue.poll();

        if (command == null)
            return;

        if (command.startsWith("/"))
            command = command.substring(1);

        MC.getNetworkHandler().sendChatCommand(command);
        lastCommandSentAt = System.currentTimeMillis();
    }

    private static void reset() {
        joinedAt = 0;
        lastCommandSentAt = 0;
        commandQueue = new LinkedList<>();
    }
}
