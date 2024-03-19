package net.xolt.sbutils.systems;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.AutoAdvert;
import net.xolt.sbutils.feature.features.AutoKit;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import static net.xolt.sbutils.SbUtils.MC;

public class ServerDetector {

    private SbServer currentServer;
    private boolean receivedCommandTree;
    private boolean receivedTabHeader;
    private boolean receivedAddress;
    private String tabHeader;
    private InetAddress skyblockAddress;

    public void onPlayerListHeader(String header) {
        tabHeader = header;
        receivedTabHeader = true;
        if (receivedCommandTree && receivedAddress) {
            updateServer(determineServer(tabHeader, skyblockAddress));
        }
    }

    public void afterCommandTree() {
        receivedCommandTree = true;
        if (receivedTabHeader && receivedAddress) {
            updateServer(determineServer(tabHeader, skyblockAddress));
        }
    }

    public void onReceiveSkyblockAddress(InetAddress address) {
        this.skyblockAddress = address;
        receivedAddress = true;
        if (receivedTabHeader && receivedCommandTree) {
            updateServer(determineServer(tabHeader, skyblockAddress));
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
        getCurrentSkyblockAddress();
    }

    private void getCurrentSkyblockAddress() {
        Thread socketThread = new Thread(() -> {
            InetAddress result = null;
            try {
                result = InetAddress.getByName("server.skyblock.net");
            } catch (Exception e) {
                SbUtils.LOGGER.error("Failed to retrieve current Skyblock ip address.");
            }
            this.onReceiveSkyblockAddress(result);
        });
        socketThread.start();
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
        receivedAddress = false;
        tabHeader = null;
        skyblockAddress = null;
    }

    private static SbServer determineServer(String tabHeader, InetAddress skyblockAddress) {
        ClientPacketListener connection = MC.getConnection();
        if (connection == null || !(connection.getConnection().getRemoteAddress() instanceof InetSocketAddress address) || !address.getAddress().equals(skyblockAddress)) {
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
        SbUtils.FEATURES.get(AutoKit.class).onSwitchServer();
    }

    public enum SbServer {
        HUB("Hub"),
        SKYWARS("Skywars"),
        SKYBLOCK("Skyblock"),
        ECONOMY("Economy"),
        CLASSIC("Classic");

        String name;

        SbServer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
