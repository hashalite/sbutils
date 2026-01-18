package net.xolt.sbutils.api;

import net.xolt.sbutils.api.response.PlayerResponse;
import net.xolt.sbutils.api.response.StaffMemberResponse;
import net.xolt.sbutils.api.response.TraderResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    protected DefaultHttpClient http;

    public ApiClient() {
        this.http = new DefaultHttpClient("https://api.skyblock.net", 20);
    }

    public CompletableFuture<PlayerResponse> getPlayer(UUID uuid) {
        String path = String.format("/player/%s", uuid.toString());
        return http.get(path, PlayerResponse.class);
    }

    public CompletableFuture<TraderResponse> getTrader(String gamemodeId) {
        String path = String.format("/gamemode/%s/traders", gamemodeId);
        return http.get(path, TraderResponse.class);
    }

    public CompletableFuture<StaffMemberResponse[]> getStaff() {
        String path = "/staff";
        return http.get(path, StaffMemberResponse[].class);
    }
}
