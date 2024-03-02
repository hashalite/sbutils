package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;

import java.util.List;
import java.util.Optional;

import static net.xolt.sbutils.SbUtils.MC;

public class DeathCoords extends Feature {

    public DeathCoords() {
        super("deathCoords", "deathcoords", "dcoords");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return null;
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> deathCoordsNode = dispatcher.register(
                CommandHelper.runnable(command, DeathCoords::onDeathCoordsCommand)
        );
        registerAlias(dispatcher, deathCoordsNode);
    }

    private static void onDeathCoordsCommand() {
        if (MC.player == null)
            return;

        Optional<GlobalPos> lastDeathPosOptional = MC.player.getLastDeathLocation();
        if (lastDeathPosOptional.isEmpty()) {
            ChatUtils.printMessage("message.sbutils.deathCoords.noDeaths");
            return;
        }

        GlobalPos lastDeathGlobalPos = lastDeathPosOptional.get();
        BlockPos lastDeathPos = lastDeathGlobalPos.pos();
        ChatUtils.printWithPlaceholders("message.sbutils.deathCoords.deathCoords", lastDeathGlobalPos.dimension().location().toShortLanguageKey(), lastDeathPos.getX(), lastDeathPos.getY(), lastDeathPos.getZ());
    }
}
