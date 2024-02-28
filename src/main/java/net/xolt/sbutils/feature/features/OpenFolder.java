package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.ChatUtils;

import java.util.List;

public class OpenFolder extends Feature {

    private static final String COMMAND = "openfolder";
    private static final String ALIAS = "dir";

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> openFolderNode = dispatcher.register(
                CommandHelper.runnable(COMMAND, OpenFolder::onOpenFolderCommand)
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(openFolderNode));
    }

    private static void onOpenFolderCommand() {
        IOHandler.openModDirectory();
        ChatUtils.printMessage("message.sbutils.openFolder.folderOpened");
    }
}
