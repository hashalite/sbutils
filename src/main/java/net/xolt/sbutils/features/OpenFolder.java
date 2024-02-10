package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

import java.util.List;

public class OpenFolder {

    private static final String COMMAND = "openfolder";
    private static final String ALIAS = "dir";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
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
        Messenger.printMessage("message.sbutils.openFolder.folderOpened");
    }
}
