package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.ChatUtils;

import java.util.List;

public class OpenFolder extends Feature<ModConfig> {

    public OpenFolder() {
        super("sbutils", "openFolder", "openfolder", "dir");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return null;
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> openFolderNode = dispatcher.register(
                CommandHelper.runnable(command, OpenFolder::onOpenFolderCommand)
        );
        registerAlias(dispatcher, openFolderNode);
    }

    private static void onOpenFolderCommand() {
        IOHandler.openModDirectory();
        ChatUtils.printMessage("message.sbutils.openFolder.folderOpened");
    }
}
