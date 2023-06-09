package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.List;
import java.util.Optional;

import static net.xolt.sbutils.SbUtils.MC;

public class DeathCoords {

    private static final String COMMAND = "deathcoords";
    private static final String ALIAS = "dcoords";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> deathCoordsNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context ->
                        onDeathCoordsCommand()
                ));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(deathCoordsNode));
    }

    private static int onDeathCoordsCommand() {
        if (MC.player == null) {
            return Command.SINGLE_SUCCESS;
        }

        Optional<GlobalPos> lastDeathPosOptional = MC.player.getLastDeathPos();
        if (lastDeathPosOptional.isEmpty()) {
            Messenger.printMessage("message.sbutils.deathCoords.noDeaths");
            return Command.SINGLE_SUCCESS;
        }

        GlobalPos lastDeathGlobalPos = lastDeathPosOptional.get();
        BlockPos lastDeathPos = lastDeathGlobalPos.getPos();
        Messenger.printWithPlaceholders("message.sbutils.deathCoords.deathCoords", lastDeathGlobalPos.getDimension().getValue().toShortTranslationKey(), lastDeathPos.getX(), lastDeathPos.getY(), lastDeathPos.getZ());
        return Command.SINGLE_SUCCESS;
    }
}
