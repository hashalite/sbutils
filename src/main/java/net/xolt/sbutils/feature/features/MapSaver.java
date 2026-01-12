package net.xolt.sbutils.feature.features;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.EntityHitResult;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.mixins.MapInstanceAccessor;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.FileUtils;
import net.xolt.sbutils.util.InvUtils;
//? if >=1.21 {
import net.minecraft.client.resources.MapTextureManager;
//? } else
//import net.minecraft.client.gui.MapRenderer;
//? if >1.20.4 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.saveddata.maps.MapId;
//? }

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class MapSaver extends Feature<ModConfig> {
    public MapSaver() {
        super("sbutils", "mapsaver", "savemap", "smap");
    }

    @Override public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
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

        //? if >1.20.4 {
        MapId mapId = map.get(DataComponents.MAP_ID);
        //? } else
        //Integer mapId = MapItem.getMapId(map);



        if (mapId == null) {
            ChatUtils.printMessage("message.sbutils.mapSaver.saveFailed");
            return;
        }
        int mapIdInt =
                //? if >1.20.4 {
                mapId.id();
                //? } else
                //mapId;

        MapItemSavedData mapData = MapItem.getSavedData(map, MC.level);
        //? if >=1.21 {
        MapTextureManager.MapInstance mapInstance = MC.getMapTextureManager().getOrCreateMapInstance(mapId, mapData);
        //? } else
        //MapRenderer.MapInstance mapInstance = MC.gameRenderer.getMapRenderer().getOrCreateMapInstance(mapId, mapData);
        NativeImage image = ((MapInstanceAccessor)mapInstance).getTexture().getPixels();
        String servername = null;
        ServerData serverData =
                //? if >=1.19.4 {
                MC.getConnection() != null ? MC.getConnection().getServerData() : null;
                //? } else
                //MC.getCurrentServer();
        if (serverData != null)
            servername = serverData.ip;
        else if (MC.getSingleplayerServer() != null)
            servername = MC.getSingleplayerServer().getWorldData().getLevelName() + " (Singleplayer)";
        else
            servername = "unknown";
        if (!FileUtils.saveMapImage(mapIdInt, servername, image)) {
            ChatUtils.printMessage("message.sbutils.mapSaver.saveFailed");
            return;
        }
        ChatUtils.printMessage("message.sbutils.mapSaver.saveSuccess");
    }

    private static ItemStack getTargetMap() {
        if (MC.player == null)
            return null;

        ItemStack held = InvUtils.getSelectedItem(MC.player);
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
