package net.xolt.sbutils.systems;

import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.AutoAdvert;
import net.xolt.sbutils.feature.features.AutoKit;

import static net.xolt.sbutils.SbUtils.MC;

public class ServerDetector {

    private SbServer currentServer;
    private long lastRequestTs;

    public void onDisconnect() {
        reset();
    }

    public void onJoinGame() {
        reset();
        fetchServer();
    }

    public void fetchServer() {
        assert MC.player != null;
        final long requestTs = System.currentTimeMillis();
        lastRequestTs = requestTs;
        SbUtils.API_CLIENT.getPlayer(MC.player.getUUID())
                .thenAccept((playerInfo) -> {
                    updateServer(SbServer.get(playerInfo.status.switchGamemode), requestTs);
                });
    }

    private void updateServer(SbServer server, long requestTs) {
        if (currentServer == server || requestTs != lastRequestTs)
            return;

        currentServer = server;
        onSwitchServer(server);
    }

    public SbServer getCurrentServer() {
        return currentServer;
    }

    public String getCurrentGamemode() {
        return currentServer.gamemodeId;
    }

    public boolean isOnSkyblock() {
        return currentServer == SbServer.SKYBLOCK || currentServer == SbServer.ECONOMY || currentServer == SbServer.CLASSIC;
    }

    private void reset() {
        currentServer = null;
    }

    public static void onSwitchServer(SbServer server) {
        SbUtils.FEATURES.get(AutoAdvert.class).onSwitchServer();
        SbUtils.FEATURES.get(AutoKit.class).onSwitchServer();
    }

    public enum SbServer {
        SKYBLOCK("Skyblock", "skyblock"),
        ECONOMY("Economy", "economy"),
        CLASSIC("Classic", "classic"),
        SKYWARS("Skywars", "skywars"),
        HUB1("Hub 1", "sb-hub-1"),
        HUB2("Hub 2", "sb-hub-2");

        String name;
        String gamemodeId;

        SbServer(String name, String gamemodeId) {
            this.name = name;
            this.gamemodeId = gamemodeId;
        }

        public static SbServer get(String gamemodeId) {
            for (SbServer sbServer : SbServer.values()) {
                if (gamemodeId.equals(sbServer.gamemodeId))
                    return sbServer;
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public String getGamemodeId() {return gamemodeId;}
    }
}
