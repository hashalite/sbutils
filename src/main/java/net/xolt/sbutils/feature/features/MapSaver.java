package net.xolt.sbutils.feature.features;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.EntityHitResult;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.mixins.MapInstanceAccessor;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.IOHandler;

import javax.swing.text.html.parser.Entity;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class MapSaver extends Feature {
    public MapSaver() {
        super("mapsaver", "savemap", "smap");
    }

    @Override public List<? extends ConfigBinding<?>> getConfigBindings() {
        return null;
    }

    @Override public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> saveMapNode = dispatcher.register(
                CommandHelper.runnable(command, MapSaver::onSaveMap)
        );
        registerAlias(dispatcher, saveMapNode);
    }

    private static void onSaveMap() {
        if (MC.player == null) {
            ChatUtils.printMessage("message.sbutils.mapSaver.saveFailed");
            return;
        }
        ItemStack map = getTargetMap();
        if (map == null) {
            ChatUtils.printMessage("message.sbutils.mapSaver.notHoldingOrInFrame");
            return;
        }
        Integer mapId = MapItem.getMapId(map);
        if (mapId == null) {
            ChatUtils.printMessage("message.sbutils.mapSaver.saveFailed");
            return;
        }
        MapItemSavedData mapData = MapItem.getSavedData(mapId, MC.level);
        MapRenderer.MapInstance mapInstance = MC.gameRenderer.getMapRenderer().getOrCreateMapInstance(mapId, mapData);
        NativeImage image = ((MapInstanceAccessor)mapInstance).getTexture().getPixels();
        if (!IOHandler.saveMapImage(mapId, image)) {
            ChatUtils.printMessage("message.sbutils.mapSaver.saveFailed");
            return;
        }
        ChatUtils.printMessage("message.sbutils.mapSaver.saveSuccess");
    }

    private static ItemStack getTargetMap() {
        ItemStack held = MC.player.getInventory().getSelected();
        if (held.getItem() == Items.FILLED_MAP)
            return held;
        ItemStack framed = null;
        if (MC.hitResult instanceof EntityHitResult hitRes && hitRes.getEntity() instanceof ItemFrame frame)
            framed = frame.getItem();
        if (framed != null && framed.getItem() == Items.FILLED_MAP)
            return framed;
        return null;
    }
}
