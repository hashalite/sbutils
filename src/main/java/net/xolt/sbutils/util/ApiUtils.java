package net.xolt.sbutils.util;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xolt.sbutils.systems.ServerDetector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ApiUtils {
    private static final String TRADER_ENDPOINT = "https://api.skyblock.net/traders";

    public static void getWanderingTrades(ServerDetector.SbServer server, Consumer<List<ItemStack>> onResponse) {
        String serverParam = server == ServerDetector.SbServer.ECONOMY ? "?server=economy" : "?server=skyblock";
        String withServer = TRADER_ENDPOINT + serverParam;
        apiRequest(withServer, (response) -> {
            List<ItemStack> result = new ArrayList<>();
            if (response == null) {
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

    public static void apiRequest(String stringUrl, Consumer<String> onResponse) {
        Thread apiThread = new Thread(() -> {
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader((new InputStreamReader(connection.getInputStream())));
                    String line;
                    StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null)
                        builder.append(line);
                    reader.close();
                    onResponse.accept(builder.toString());
                    return;
                }
                connection.disconnect();
            } catch (Exception ignored) {}
            onResponse.accept(null);
        });
        apiThread.start();
    }
}
