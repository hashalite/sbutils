package net.xolt.sbutils.api.response;

public class PlayerResponse {
    public String discordId;
    public String favouriteGamemode;
    public String forumsId;
    public String mojangUsername;
    public String mojangUsernamePretty;
    public String nextGamemode;
    public PlayerStatus status;
    public String type;
    public long updatedTs;

    public static class PlayerStatus {
        public long connectFirstTs;
        public String connectGamemode;
        public long connectTs;
        public String connectVersion;
        public String disconnectGamemode;
        public long disconnectTs;
        public String switchGamemode;
        public long switchGamemodeTs;
    }
}
