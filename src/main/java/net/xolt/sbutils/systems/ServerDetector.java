package net.xolt.sbutils.systems;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.AutoAdvert;
import net.xolt.sbutils.feature.features.AutoKit;
import net.xolt.sbutils.util.DnsUtils;

import javax.naming.directory.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.xolt.sbutils.SbUtils.MC;

public class ServerDetector {

    private static final String SKYBLOCK_ADDRESS = "skyblock.net";
    private static final String SRV_QUERY = "_minecraft._tcp." + SKYBLOCK_ADDRESS;

    private SbServer currentServer;
    private long lastRequestTs;

    public void onDisconnect() {
        reset();
    }

    public void onJoinGame() {
        reset();
        resolveServer()
                .thenAccept(this::updateServer)
                .exceptionally(e -> {
                    if (e instanceof ExpiredRequestException) {
                        SbUtils.LOGGER.info(e.getLocalizedMessage());
                    } else {
                        SbUtils.LOGGER.error("Failed to resolve server");
                        SbUtils.LOGGER.error(e.getLocalizedMessage());
                    }
                    return null;
                });
    }

    public CompletableFuture<SbServer> resolveServer() {
        final long requestTs = System.currentTimeMillis();
        lastRequestTs = requestTs;
        CompletableFuture<SbServer> result = new CompletableFuture<>();
        CompletableFuture<SbServer> sbServer = fetchGamemode();
        CompletableFuture<List<InetAddress>> addresses = lookupSkyblockIp();
        CompletableFuture.allOf(sbServer, addresses)
                .thenApply(v ->
                        validateAddress(addresses.join()) ? sbServer.join() : null
                )
                .thenAccept(server -> {
                    if (this.lastRequestTs != requestTs) {
                        //Request expired
                        result.completeExceptionally(new ExpiredRequestException());
                        return;
                    }
                    result.complete(server);
                })
                .exceptionally(e -> {
                    SbUtils.LOGGER.error(e.getLocalizedMessage());
                    return null;
                });
        return result;
    }

    public static boolean validateAddress(List<InetAddress> addresses) {
        ClientPacketListener connection = MC.getConnection();
        return connection != null && connection.getConnection().getRemoteAddress() instanceof InetSocketAddress address && addresses.contains(address.getAddress());
    }

    public static CompletableFuture<List<InetAddress>> lookupSkyblockIp() {
        List<CompletableFuture<List<InetAddress>>> aLookups = new ArrayList<>();
        return CompletableFuture.supplyAsync(() -> DnsUtils.srvLookup(SRV_QUERY))
                .thenCompose(srvRecords -> {
                    for (String srvRecord : srvRecords) {
                        aLookups.add(CompletableFuture.supplyAsync(() -> DnsUtils.aRecordLookup(srvRecord, false)));
                        aLookups.add(CompletableFuture.supplyAsync(() -> DnsUtils.aRecordLookup(srvRecord, true)));
                    }

                    return CompletableFuture.allOf(aLookups.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                List<InetAddress> result = new ArrayList<>();
                                aLookups.forEach(aLookup -> result.addAll(aLookup.join()));
                                return result;
                            });
                });
    }


    // Gets the gamemodeId the player was last seen on
    public static CompletableFuture<SbServer> fetchGamemode() {
        assert MC.player != null;
        return SbUtils.API_CLIENT.getPlayer(MC.player.getUUID())
                .exceptionally(e -> {
                    SbUtils.LOGGER.error(e.getLocalizedMessage());
                    return null;
                })
                .thenApply((playerInfo) -> SbServer.get(playerInfo.status.switchGamemode));
    }

    private void updateServer(SbServer server) {
        if (currentServer == server)
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

        private final String name;
        private final String gamemodeId;

        SbServer(String name, String gamemodeId) {
            this.name = name;
            this.gamemodeId = gamemodeId;
        }

        public static SbServer get(String gamemodeId) {
            for (SbServer sbServer : SbServer.values()) {
                if (sbServer.gamemodeId.equals(gamemodeId))
                    return sbServer;
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public String getGamemodeId() {return gamemodeId;}
    }

    private class ExpiredRequestException extends Exception {
        public ExpiredRequestException() {
            super("Ignoring result of expired server resolution");
        }
    }
}
