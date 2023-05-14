package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCommand {

    private static final String COMMAND = "autocmd";
    private static final String ALIAS = "acmd";

    private static long lastCmdSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoCommandNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoCommandEnabled = !ModConfig.INSTANCE.getConfig().autoCommandEnabled;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autocommand", ModConfig.INSTANCE.getConfig().autoCommandEnabled);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("info")
                        .executes(context -> {
                            Messenger.printAutoCommandInfo(ModConfig.INSTANCE.getConfig().autoCommandEnabled, delayLeft());
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(ClientCommandManager.literal("command")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoCommand", ModConfig.INSTANCE.getConfig().autoCommand);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoCommand = StringArgumentType.getString(context, "command");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoCommand", ModConfig.INSTANCE.getConfig().autoCommand);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoCommandDelay", ModConfig.INSTANCE.getConfig().autoCommandDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoCommandDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoCommandDelay", ModConfig.INSTANCE.getConfig().autoCommandDelay);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute("autocmd", context.getSource())
                )
                .redirect(autoCommandNode));
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().autoCommandEnabled || MC.getNetworkHandler() == null) {
            return;
        }

        if (delayLeft() == 0) {
            sendCommand();
        }
    }

    private static void sendCommand() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        String command = ModConfig.INSTANCE.getConfig().autoCommand;
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        MC.getNetworkHandler().sendChatCommand(command);
        lastCmdSentAt = System.currentTimeMillis();
    }

    private static int delayLeft() {
        long delay = (long)(ModConfig.INSTANCE.getConfig().autoCommandDelay * 1000.0);
        return (int)Math.max((delay - (System.currentTimeMillis() - lastCmdSentAt)), 0L);
    }
}
