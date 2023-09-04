package net.xolt.sbutils.features.common;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.xolt.sbutils.features.AutoAdvert;
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
            currentServer = null;
            onSwitchServer();
            return;
        }

        for (CommandNode<CommandSource> node : MC.getNetworkHandler().getCommandDispatcher().getRoot().getChildren()) {
            switch (node.getName()) {
                case "crophoppers:crophoppers":
                    currentServer  = SbServer.ECONOMY;
                    onSwitchServer();
                    return;
                case "mineversesidebar:sidebar":
                    currentServer = SbServer.HUB;
                    onSwitchServer();
                    return;
                case "plugman:plugman":
                    currentServer = SbServer.SKYBLOCK;
                    onSwitchServer();
                    return;
            }
        }

        if (tabHeader.contains("Skyblock Classic")) {
            currentServer = SbServer.CLASSIC;
            onSwitchServer();
            return;
        }

        if (tabHeader.contains("SkyWars")) {
            currentServer = SbServer.SKYWARS;
            onSwitchServer();
            return;
        }

        currentServer = null;
        onSwitchServer();
    }

    public static void onDisconnect() {
        resetServer();
    }

    public static void onJoinGame() {
        resetServer();
    }

    public static void onSwitchServer() {
        AutoAdvert.refreshPrevAdlist();
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
