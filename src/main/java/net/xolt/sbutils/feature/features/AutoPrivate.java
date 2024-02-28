package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.item.SignItem;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.feature.Feature;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoPrivate extends Feature {

    private static final String COMMAND = "autoprivate";
    private static final String ALIAS = "ap";

    private boolean sneaked;

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoPrivateNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoPrivate", () -> ModConfig.HANDLER.instance().autoPrivate.enabled, (value) -> ModConfig.HANDLER.instance().autoPrivate.enabled = value)
                        .then(CommandHelper.stringList("names", "name", "autoPrivate.names", false, 2, false, false, () -> ModConfig.HANDLER.instance().autoPrivate.names, (value) -> ModConfig.HANDLER.instance().autoPrivate.names = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoPrivateNode));
    }

    public void onInteractBlock() {
        if (MC.player == null || MC.getConnection() == null)
            return;
        if (!ModConfig.HANDLER.instance().autoPrivate.enabled || !(MC.player.getMainHandItem().getItem() instanceof SignItem))
            return;
        MC.getConnection().send(new ServerboundPlayerCommandPacket(MC.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        sneaked = true;
    }

    public void afterInteractBlock() {
        if (!sneaked || MC.getConnection() == null || MC.player == null)
            return;
        MC.getConnection().send(new ServerboundPlayerCommandPacket(MC.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        sneaked = false;
    }

    public static boolean onSignEditorOpen(ClientboundOpenSignEditorPacket packet) {
        if (!ModConfig.HANDLER.instance().autoPrivate.enabled || !packet.isFrontText()) {
            return false;
        }
        return updateSign(packet);
    }

    private static boolean updateSign(ClientboundOpenSignEditorPacket packet) {
        if (MC.getConnection() == null || MC.player == null) {
            return false;
        }

        List<String> names = ModConfig.HANDLER.instance().autoPrivate.names;
        String[] lines = {"", ""};

        for (int i = 0; i < Math.min(names.size(), lines.length); i++) {
            lines[i] = names.get(i);
        }

        MC.getConnection().send(new ServerboundSignUpdatePacket(packet.getPos(), true, "[private]", MC.player.getName().getString(), lines[0], lines[1]));
        return true;
    }
}
