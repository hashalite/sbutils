package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.LimitedList;
import net.xolt.sbutils.util.Messenger;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoPrivate {

    private static final String COMMAND = "autoprivate";
    private static final String ALIAS = "ap";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoPrivateNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoPrivate = !ModConfig.INSTANCE.getConfig().autoPrivate;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autoprivate", ModConfig.INSTANCE.getConfig().autoPrivate);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("names")
                        .executes(context -> {
                            Messenger.printListSetting("message.sbutils.autoPrivate.names", ModConfig.INSTANCE.getConfig().autoPrivateNames);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                        .executes(context ->
                                                onAddNameCommand(StringArgumentType.getString(context, "name"))
                                        )))
                        .then(ClientCommandManager.literal("del")
                                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                        .executes(context ->
                                                onDelNameCommand(StringArgumentType.getString(context, "name"))
                                        )))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoPrivateNode));
    }

    private static int onAddNameCommand(String name) {
        List<String> names = new LimitedList<>(2, ModConfig.INSTANCE.getConfig().autoPrivateNames);

        if (names.size() >= 2) {
            Messenger.printMessage("message.sbutils.autoPrivate.maxNamesSet");
            return Command.SINGLE_SUCCESS;
        }

        if (names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameAddFail", name);
            return Command.SINGLE_SUCCESS;
        }

        names.add(name);
        ModConfig.INSTANCE.getConfig().autoPrivateNames = names;
        ModConfig.INSTANCE.save();
        Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameAddSuccess", name);
        return Command.SINGLE_SUCCESS;
    }

    private static int onDelNameCommand(String name) {
        List<String> names = new LimitedList<>(2, ModConfig.INSTANCE.getConfig().autoPrivateNames);

        if (ModConfig.INSTANCE.getConfig().autoPrivateNames.size() == 0) {
            Messenger.printMessage("message.sbutils.autoPrivate.noNamesSet");
            return Command.SINGLE_SUCCESS;
        }

        if (!names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameDelFail", name);
            return Command.SINGLE_SUCCESS;
        }

        names.remove(name);
        ModConfig.INSTANCE.getConfig().autoPrivateNames = names;
        ModConfig.INSTANCE.save();
        Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameDelSuccess", name);
        return Command.SINGLE_SUCCESS;
    }

    public static boolean onSignEditorOpen(SignEditorOpenS2CPacket packet) {
        if (!ModConfig.INSTANCE.getConfig().autoPrivate || !packet.isFront()) {
            return false;
        }
        return updateSign(packet);
    }

    private static boolean updateSign(SignEditorOpenS2CPacket packet) {
        if (MC.getNetworkHandler() == null) {
            return false;
        }

        List<String> names = ModConfig.INSTANCE.getConfig().autoPrivateNames;
        String[] lines = {"", ""};

        for (int i = 0; i < Math.min(names.size(), lines.length); i++) {
            lines[i] = names.get(i);
        }

        MC.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(packet.getPos(), true, "[private]", "", lines[0], lines[1]));
        return true;
    }
}
