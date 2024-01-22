package net.xolt.sbutils.features.common;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.xolt.sbutils.features.AutoAdvert;
import net.xolt.sbutils.features.AutoKit;
import net.xolt.sbutils.util.RegexFilters;

import static net.xolt.sbutils.SbUtils.MC;

public class ServerDetector {

    public static SbServer currentServer;
    private static boolean receivedCommandTree = false;
    private static boolean receivedTabHeader = false;
    private static String tabHeader;

    public static void onPlayerListHeader(String header) {
        tabHeader = header;
        receivedTabHeader = true;
        if (receivedCommandTree) {
            determineServer();
        }
    }

    public static void afterCommandTree() {
        receivedCommandTree = true;
        if (receivedTabHeader) {
            determineServer();
        }
    }

    private static void determineServer() {
        if (!receivedCommandTree || !receivedTabHeader || MC.getNetworkHandler() == null
                || !RegexFilters.addressFilter.matcher(MC.getNetworkHandler().getConnection().getAddress().toString()).matches()) {
            updateServer(null);
            return;
        }

        for (CommandNode<CommandSource> node : MC.getNetworkHandler().getCommandDispatcher().getRoot().getChildren()) {
            switch (node.getName()) {
                case "crophoppers:crophoppers":
                    updateServer(SbServer.ECONOMY);
                    return;
                case "mineversesidebar:sidebar":
                    updateServer(SbServer.HUB);
                    return;
                case "plugman:plugman":
                    updateServer(SbServer.SKYBLOCK);
                    return;
            }
        }

        if (tabHeader.contains("Skyblock Classic")) {
            updateServer(SbServer.CLASSIC);
            return;
        }

        if (tabHeader.contains("SkyWars")) {
            updateServer(SbServer.SKYWARS);
            return;
        }

        updateServer(null);
    }

    private static void updateServer(SbServer server) {
        if (currentServer == server)
            return;
        currentServer = server;
        onSwitchServer(server);
    }

    public static void onDisconnect() {
        resetServer();
    }

    public static void onJoinGame() {
        resetServer();
    }

    public static void onSwitchServer(SbServer server) {
        AutoAdvert.refreshPrevAdlist();
        AutoKit.onSwitchServer(server);
    }

    public static boolean isOnSkyblock() {
        return currentServer == SbServer.SKYBLOCK || currentServer == SbServer.ECONOMY || currentServer == SbServer.CLASSIC;
    }

    private static void resetServer() {
        currentServer = null;
        receivedCommandTree = false;
        receivedTabHeader = false;
        tabHeader = null;
    }

    public enum SbServer {
        HUB,
        SKYWARS,
        SKYBLOCK,
        ECONOMY,
        CLASSIC;
    }
}
