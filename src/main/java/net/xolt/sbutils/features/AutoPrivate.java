package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.SignItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoPrivate {

    private static final String COMMAND = "autoprivate";
    private static final String ALIAS = "ap";

    private static boolean sneaked;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoPrivateNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoPrivate", () -> ModConfig.HANDLER.instance().autoPrivate.enabled, (value) -> ModConfig.HANDLER.instance().autoPrivate.enabled = value)
                        .then(CommandHelper.stringList("names", "name", "autoPrivate.names", 2, false, false, () -> ModConfig.HANDLER.instance().autoPrivate.names, (value) -> ModConfig.HANDLER.instance().autoPrivate.names = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoPrivateNode));
    }

    public static void onInteractBlock() {
        if (MC.player == null || MC.getNetworkHandler() == null)
            return;
        if (!ModConfig.HANDLER.instance().autoPrivate.enabled || !(MC.player.getMainHandStack().getItem() instanceof SignItem))
            return;
        MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        sneaked = true;
    }

    public static void afterInteractBlock() {
        if (!sneaked || MC.getNetworkHandler() == null || MC.player == null)
            return;
        MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        sneaked = false;
    }

    public static boolean onSignEditorOpen(SignEditorOpenS2CPacket packet) {
        if (!ModConfig.HANDLER.instance().autoPrivate.enabled || !packet.isFront()) {
            return false;
        }
        return updateSign(packet);
    }

    private static boolean updateSign(SignEditorOpenS2CPacket packet) {
        if (MC.getNetworkHandler() == null || MC.player == null) {
            return false;
        }

        List<String> names = ModConfig.HANDLER.instance().autoPrivate.names;
        String[] lines = {"", ""};

        for (int i = 0; i < Math.min(names.size(), lines.length); i++) {
            lines[i] = names.get(i);
        }

        MC.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(packet.getPos(), true, "[private]", MC.player.getName().getString(), lines[0], lines[1]));
        return true;
    }
}
