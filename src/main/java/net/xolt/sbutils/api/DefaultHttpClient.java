package net.xolt.sbutils.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class DefaultHttpClient {

    private HttpClient http;
    private URI baseUri;
    private Gson mapper;

    public DefaultHttpClient(String baseUri, int timeoutSeconds) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        this.baseUri = URI.create(baseUri);
        this.mapper = new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.IDENTITY)
                .create();
    }

    public <T> CompletableFuture<T> get(String path, Class<T> responseType) {
        HttpRequest request = requestBuilder(path)
                .GET()
                .build();

        return sendAsync(request, responseType);
    }

    public <T> CompletableFuture<T> post(String path, Object body, Class<T> responseType) {
        String json = mapper.toJson(body);

        HttpRequest request = requestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return sendAsync(request, responseType);
    }

    private <T> CompletableFuture<T> sendAsync(HttpRequest request, Class<T> responseType) {
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    int status = response.statusCode();
                    String body = response.body();

                    if (status != 200) {
                        return CompletableFuture.failedFuture(
                                new IOException("Got HTTP Status " + status + " from " + response.uri())
                        );
                    }

                    if (body == null || body.isBlank()) {
                        return CompletableFuture.completedFuture(null);
                    }

                    try {
                        T value = mapper.fromJson(body, responseType);
                        return CompletableFuture.completedFuture(value);
                    } catch (JsonSyntaxException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(baseUri.resolve(path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }
}
