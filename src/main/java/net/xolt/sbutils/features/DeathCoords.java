package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.Messenger;

import java.util.List;
import java.util.Optional;

import static net.xolt.sbutils.SbUtils.MC;

public class DeathCoords {

    private static final String COMMAND = "deathcoords";
    private static final String ALIAS = "dcoords";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> deathCoordsNode = dispatcher.register(
                CommandHelper.runnable(COMMAND, DeathCoords::onDeathCoordsCommand)
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(deathCoordsNode));
    }

    private static void onDeathCoordsCommand() {
        if (MC.player == null) {
            return;
        }

        Optional<GlobalPos> lastDeathPosOptional = MC.player.getLastDeathLocation();
        if (lastDeathPosOptional.isEmpty()) {
            Messenger.printMessage("message.sbutils.deathCoords.noDeaths");
            return;
        }

        GlobalPos lastDeathGlobalPos = lastDeathPosOptional.get();
        BlockPos lastDeathPos = lastDeathGlobalPos.pos();
        Messenger.printWithPlaceholders("message.sbutils.deathCoords.deathCoords", lastDeathGlobalPos.dimension().location().toShortLanguageKey(), lastDeathPos.getX(), lastDeathPos.getY(), lastDeathPos.getZ());
    }
}
