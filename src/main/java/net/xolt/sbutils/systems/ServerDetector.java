package net.xolt.sbutils.systems;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.AutoAdvert;
import net.xolt.sbutils.feature.features.AutoKit;
import net.xolt.sbutils.util.RegexFilters;

import static net.xolt.sbutils.SbUtils.MC;

public class ServerDetector {

    private SbServer currentServer;
    private boolean receivedCommandTree;
    private boolean receivedTabHeader;
    private String tabHeader;

    public void onPlayerListHeader(String header) {
        tabHeader = header;
        receivedTabHeader = true;
        if (receivedCommandTree) {
            updateServer(determineServer(tabHeader));
        }
    }

    public void afterCommandTree() {
        receivedCommandTree = true;
        if (receivedTabHeader) {
            updateServer(determineServer(tabHeader));
        }
    }

    private void updateServer(SbServer server) {
        if (currentServer == server)
            return;
        currentServer = server;
        onSwitchServer(server);
    }

    public void onDisconnect() {
        reset();
    }

    public void onJoinGame() {
        reset();
    }

    public SbServer getCurrentServer() {
        return currentServer;
    }

    public boolean isOnSkyblock() {
        return currentServer == SbServer.SKYBLOCK || currentServer == SbServer.ECONOMY || currentServer == SbServer.CLASSIC;
    }

    private void reset() {
        currentServer = null;
        receivedCommandTree = false;
        receivedTabHeader = false;
        tabHeader = null;
    }

    private static SbServer determineServer(String tabHeader) {
        ClientPacketListener connection = MC.getConnection();
        if (connection == null || !RegexFilters.addressFilter.matcher(connection.getConnection().getRemoteAddress().toString()).matches()) {
            return null;
        }

        for (CommandNode<SharedSuggestionProvider> node : connection.getCommands().getRoot().getChildren()) {
            switch (node.getName()) {
                case "crophoppers:crophoppers":
                    return SbServer.ECONOMY;
                case "mineversesidebar:sidebar":
                    return SbServer.HUB;
                case "plugman:plugman":
                    return SbServer.SKYBLOCK;
            }
        }

        if (tabHeader.contains("Skyblock Classic")) {
            return SbServer.CLASSIC;
        }

        if (tabHeader.contains("SkyWars")) {
            return SbServer.SKYWARS;
        }

        return null;
    }

    public static void onSwitchServer(SbServer server) {
        SbUtils.FEATURES.get(AutoAdvert.class).onSwitchServer();
        SbUtils.FEATURES.get(AutoKit.class).onSwitchServer(server);
    }

    public enum SbServer {
        HUB,
        SKYWARS,
        SKYBLOCK,
        ECONOMY,
        CLASSIC;
    }
}
