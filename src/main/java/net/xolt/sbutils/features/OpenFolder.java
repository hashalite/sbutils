package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.xolt.sbutils.util.IOHandler;
import net.xolt.sbutils.util.Messenger;

public class OpenFolder {

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> openFolderNode = dispatcher.register(ClientCommandManager.literal("openfolder")
                .executes(openFolder ->
                        onOpenFolderCommand()
                ));

        dispatcher.register(ClientCommandManager.literal("dir")
                .executes(context ->
                        dispatcher.execute("openfolder", context.getSource())
                )
                .redirect(openFolderNode));
    }

    private static int onOpenFolderCommand() {
        if (IOHandler.openModDirectory()) {
            Messenger.printMessage("message.sbutils.openFolder.folderOpened");
        } else {
            Messenger.printMessage("message.sbutils.openFolder.cantOpen");
        }

        return Command.SINGLE_SUCCESS;
    }
}
