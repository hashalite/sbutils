package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.ArrayList;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoPrivate {

    private static final String COMMAND = "autoprivate";
    private static final String ALIAS = "ap";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoPrivateNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoPrivate", () -> ModConfig.HANDLER.instance().autoPrivate.enabled, (value) -> ModConfig.HANDLER.instance().autoPrivate.enabled = value)
                        .then(CommandUtils.stringList("names", "name", "message.sbutils.autoPrivate.names",
                                () -> ModConfig.HANDLER.instance().autoPrivate.names,
                                AutoPrivate::onAddNameCommand,
                                AutoPrivate::onDelNameCommand,
                                AutoPrivate::onInsertNameCommand))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoPrivateNode));
    }

    private static void onAddNameCommand(String name) {
        List<String> names = new ArrayList<>(ModConfig.HANDLER.instance().autoPrivate.names);

        if (names.size() >= 2) {
            Messenger.printMessage("message.sbutils.autoPrivate.maxNamesSet");
            return;
        }

        if (names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameAddFail", name);
            return;
        }

        names.add(name);
        ModConfig.HANDLER.instance().autoPrivate.names = names;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameAddSuccess", name);
    }

    private static void onDelNameCommand(int index) {
        List<String> names = new ArrayList<>(ModConfig.HANDLER.instance().autoPrivate.names);

        if (ModConfig.HANDLER.instance().autoPrivate.names.isEmpty()) {
            Messenger.printMessage("message.sbutils.autoPrivate.noNamesSet");
            return;
        }

        int adjustedIndex = index - 1;
        if (adjustedIndex >= names.size() || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameInvalidIndex", index);
            return;
        }

        String name = names.remove(adjustedIndex);
        ModConfig.HANDLER.instance().autoPrivate.names = names;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameDelSuccess", name);
    }

    private static void onInsertNameCommand(int index, String name) {
        List<String> names = new ArrayList<>(ModConfig.HANDLER.instance().autoPrivate.names);

        int adjustedIndex = index - 1;
        if (adjustedIndex > Math.max(1, names.size()) || adjustedIndex < 0) {
            Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameInvalidIndex", index);
            return;
        }

        if (names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameAddFail", name);
            return;
        }

        names.add(adjustedIndex, name);
        while (names.size() > 2)
            names.remove(names.size() - 1);
        ModConfig.HANDLER.instance().autoPrivate.names = names;
        ModConfig.HANDLER.save();
        Messenger.printWithPlaceholders("message.sbutils.autoPrivate.nameAddSuccess", name);
    }

    public static boolean onSignEditorOpen(SignEditorOpenS2CPacket packet) {
        if (!ModConfig.HANDLER.instance().autoPrivate.enabled || !packet.isFront()) {
            return false;
        }
        return updateSign(packet);
    }

    private static boolean updateSign(SignEditorOpenS2CPacket packet) {
        if (MC.getNetworkHandler() == null) {
            return false;
        }

        List<String> names = ModConfig.HANDLER.instance().autoPrivate.names;
        String[] lines = {"", ""};

        for (int i = 0; i < Math.min(names.size(), lines.length); i++) {
            lines[i] = names.get(i);
        }

        MC.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(packet.getPos(), true, "[private]", "", lines[0], lines[1]));
        return true;
    }
}
