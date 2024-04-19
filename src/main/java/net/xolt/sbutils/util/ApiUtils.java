package net.xolt.sbutils.util;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.systems.ServerDetector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class ApiUtils {
    private static final String TRADER_ENDPOINT = "https://api.skyblock.net/traders";
    private static final String STAFF_ENDPOINT = "https://friends.skyblock.net/api/friends/staff";

    public static void getWanderingTrades(ServerDetector.SbServer server, Consumer<List<ItemStack>> onResponse) {
        String serverParam = server == ServerDetector.SbServer.ECONOMY ? "?server=economy" : "?server=skyblock";
        String withServer = TRADER_ENDPOINT + serverParam;
        apiRequest(withServer, (response) -> {
            List<ItemStack> result = new ArrayList<>();
            if (response == null) {
                SbUtils.LOGGER.error("Failed to retrieve wandering trader data");
                onResponse.accept(result);
                return;
            }
            Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.IDENTITY).create();
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            if (!jsonResponse.has("buyable")) {
                onResponse.accept(result);
                return;
            }
            for (JsonElement itemElement : jsonResponse.getAsJsonArray("buyable").asList()) {
                JsonObject itemObject = itemElement.getAsJsonObject();
                if (!itemObject.has("item"))
                    continue;
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemObject.get("item").getAsString().toLowerCase()));
                ItemStack itemStack = new ItemStack(item);
                result.add(itemStack);
            }
            onResponse.accept(result);
        });
    }

    public static void getStaffList(Consumer<Map<UUID, String>> onResponse) {
        apiRequest(STAFF_ENDPOINT, (response) -> {
            Map<UUID, String> result = new HashMap<>();
            if (response == null) {
                SbUtils.LOGGER.error("Failed to retrieve staff list");
                onResponse.accept(result);
                return;
            }
            Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.IDENTITY).create();
            JsonArray staffArray = gson.fromJson(response, JsonArray.class);
            if (staffArray.isEmpty()) {
                onResponse.accept(result);
                return;
            }

            for (JsonElement staff : staffArray) {
                JsonObject staffObj = staff.getAsJsonObject();
                if (!staffObj.has("uuid")) {
                    continue;
                }
                String uuidString = staffObj.get("uuid").getAsString();
                UUID uuid = UUID.fromString(uuidString);
                String position = "";
                if (staffObj.has("position")) {
                    position = staffObj.get("position").getAsString();
                }
                if (!position.isEmpty()) {
                    // Capitalize first letter of position
                    position = Character.toUpperCase(position.charAt(0)) + (position.length() > 1 ? position.substring(1) : "");
                }
                result.put(uuid, position);
            }
            onResponse.accept(result);
        });
    }

    public static void apiRequest(String stringUrl, Consumer<String> onResponse) {
        Thread apiThread = new Thread(() -> {
            String response = null;
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    connection.disconnect();
                    onResponse.accept(null);
                    return;
                }
                BufferedReader reader = new BufferedReader((new InputStreamReader(connection.getInputStream())));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    builder.append(line);
                reader.close();
                connection.disconnect();
                response = builder.toString();
            } catch (Exception e) {
                onResponse.accept(null);
            }
            onResponse.accept(response);
        });
        apiThread.start();
    }
}
