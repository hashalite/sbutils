package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class Centered extends Feature<ModConfig> {

    public Centered() {
        super("sbutils", "centered", "centered", "ctr");
    }

    @Override public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return null;
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> centeredNode = dispatcher.register(
                CommandHelper.runnable(command, Centered::onCenteredCommand)
        );
        registerAlias(dispatcher, centeredNode);
    }

    private static void onCenteredCommand() {
        if (MC.player == null) {
            return;
        }
        BlockPos pos = MC.player.blockPosition();
        if (isCentered(pos, 149)) {
            int[] extraSpace = findExtraSpace(pos, 149);
            showMapArtSuitability(149, extraSpace);
        } else if (isCentered(pos, 199)) {
            int[] extraSpace = findExtraSpace(pos, 199);
            showMapArtSuitability(199, extraSpace);
        } else {
            ChatUtils.printMessage("message.sbutils.centered.notSuitable");
        }
    }

    private static void showMapArtSuitability(int size, int[] extraSpace) {
        String expansion = size + "x" + size;
        ChatUtils.printWithPlaceholders("message.sbutils.centered.suitable", expansion);
        ChatUtils.printWithPlaceholders("message.sbutils.centered.extraSpace", extraSpace[3], extraSpace[2], extraSpace[0], extraSpace[1]);
    }

    private static boolean isCentered(BlockPos pos, int islandSize) {
        BlockPos islandCenter = findIslandCenter(pos);
        BlockPos mapCenter = findClosestMapCenter(islandCenter);
        int[] islandBounds = findIslandBounds(islandCenter, islandSize);
        return mapCenter.getX() + 63 <= islandBounds[0] &&
                mapCenter.getX() - 64 >= islandBounds[1] &&
                mapCenter.getZ() + 63 <= islandBounds[2] &&
                mapCenter.getZ() - 64 >= islandBounds[3];
    }

    private static int[] findExtraSpace(BlockPos pos, int islandSize) {
        BlockPos islandCenter = findIslandCenter(pos);
        BlockPos mapCenter = findClosestMapCenter(islandCenter);
        int[] islandBounds = findIslandBounds(islandCenter, islandSize);
        return new int[]{islandBounds[0] - (mapCenter.getX() + 63),
                        (mapCenter.getX() - 64) - islandBounds[1],
                        islandBounds[2] - (mapCenter.getZ() + 63),
                        (mapCenter.getZ() - 64) - islandBounds[3]};
    }

    private static int[] findIslandBounds(BlockPos center, int islandSize) {
        int posRadius = (int)Math.ceil((double)islandSize / 2.0);
        int negRadius = (int)Math.floor((double)islandSize / 2.0);
        return new int[]{center.getX() + posRadius,
                        center.getX() - negRadius,
                        center.getZ() + posRadius,
                        center.getZ() - negRadius};
    }

    private static BlockPos findIslandCenter(BlockPos pos) {
        BigDecimal xQuotient = BigDecimal.valueOf((double)pos.getX() / 200.0);
        BigDecimal zQuotient = BigDecimal.valueOf((double)pos.getZ() / 200.0);
        int centerX = xQuotient.setScale(0, RoundingMode.HALF_DOWN).intValue() * 200;
        int centerZ = zQuotient.setScale(0, RoundingMode.HALF_DOWN).intValue() * 200;
        return new BlockPos(centerX, 0, centerZ);
    }

    private static BlockPos findClosestMapCenter(BlockPos pos) {
        int centerX = (int)Math.round((double)pos.getX() / 128.0) * 128;
        int centerZ = (int)Math.round((double)pos.getZ() / 128.0) * 128;
        return new BlockPos(centerX, 0, centerZ);
    }
}
