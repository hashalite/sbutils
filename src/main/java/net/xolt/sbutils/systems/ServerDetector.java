package net.xolt.sbutils.systems;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.AutoAdvert;
import net.xolt.sbutils.feature.features.AutoKit;

import javax.naming.directory.*;
import javax.naming.NamingException;
import javax.naming.Context;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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

    public boolean validateAddress(List<InetAddress> addresses) {
        ClientPacketListener connection = MC.getConnection();
        return connection != null && connection.getConnection().getRemoteAddress() instanceof InetSocketAddress address && addresses.contains(address.getAddress());
    }

    public CompletableFuture<List<InetAddress>> lookupSkyblockIp() {
        List<CompletableFuture<List<InetAddress>>> aLookups = new ArrayList<>();
        return CompletableFuture.supplyAsync(ServerDetector::srvLookup)
                .thenCompose(srvRecords -> {
                    for (String srvRecord : srvRecords) {
                        aLookups.add(CompletableFuture.supplyAsync(() -> aRecordLookup(srvRecord, false)));
                        aLookups.add(CompletableFuture.supplyAsync(() -> aRecordLookup(srvRecord, true)));
                    }

                    return CompletableFuture.allOf(aLookups.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                List<InetAddress> result = new ArrayList<>();
                                aLookups.forEach(aLookup -> result.addAll(aLookup.join()));
                                return result;
                            });
                });
    }

    public static List<String> srvLookup() {
        List<String> records = dnsLookup(SRV_QUERY, "SRV");

        if (records.isEmpty()) {
            SbUtils.LOGGER.error("skyblock.net has no SRV records!");
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String record : records) {
            // SRV format: "priority weight port target"
            // Example: "0 5 25565 server.skyblock.net."
            String[] parts = record.split("\\s+");
            String target = parts[3];
            // Remove trailing dot if present
            if (target.endsWith(".")) {
                target = target.substring(0, target.length() - 1);
            }
            result.add(target);
        }
        return result;
    }

    public static List<InetAddress> aRecordLookup(String domain, boolean ipv6) {
        List<String> records = dnsLookup(domain, ipv6 ? "AAAA" : "A");

        if (records.isEmpty() && !ipv6) {
            SbUtils.LOGGER.error("The SRV target {} does not resolve to any {} address.", domain, ipv6 ? "IPv6" : "IPv4");
            return List.of();
        }

        List<InetAddress> result = new ArrayList<>();

        for (String record : records) {
            try {
                result.add(InetAddress.getByName(record));
            } catch (UnknownHostException e) {
                SbUtils.LOGGER.error(e.getLocalizedMessage());
            }
        }

        return result;
    }

    public static List<String> dnsLookup(String domain, String recordType) {
        List<String> result = new ArrayList<>();
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        DirContext ctx;
        Attributes attrs;

        try {
            ctx = new InitialDirContext(env);
            attrs = ctx.getAttributes(domain, new String[]{recordType});
        } catch (NamingException e) {
            SbUtils.LOGGER.error("Failed to perform DNS lookup on " + domain + " for record type " + recordType);
            SbUtils.LOGGER.error(e.getLocalizedMessage());
            return result;
        }

        Attribute attr = attrs.get(recordType);

        if (attr == null) {
            return result;
        }

        for (int i = 0; i < attr.size(); i++) {
            try {
                result.add((String) attr.get(i));
            } catch (NamingException e) {
                SbUtils.LOGGER.error("One record of type \"" + recordType + "\" was unable to be retrieved for " + domain);
                SbUtils.LOGGER.error(e.getExplanation());
            }
        }

        return result;
    }


    // Gets the gamemodeId the player was last seen on
    public CompletableFuture<SbServer> fetchGamemode() {
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

        final String name;
        final String gamemodeId;

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

    private class ExpiredRequestException extends Exception {
        public ExpiredRequestException() {
            super("Ignoring result of expired server resolution");
        }
    }
}
