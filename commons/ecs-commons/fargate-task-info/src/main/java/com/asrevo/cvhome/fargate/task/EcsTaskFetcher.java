package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;

public class EcsTaskFetcher {
    private static final String ecsContainerMetadataUri =
            System.getenv("ECS_CONTAINER_METADATA_URI_V4");

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static EcsTask fetch() {
        HttpRequest req =
                HttpRequest.newBuilder()
                        .uri(new URI(ecsContainerMetadataUri + "/task"))
                        .GET()
                        .build();
        return client.send(
                        req,
                        responseInfo ->
                                HttpResponse.BodySubscribers.mapping(
                                        HttpResponse.BodySubscribers.ofString(
                                                StandardCharsets.UTF_8),
                                        (String body) -> {
                                            try {
                                                return objectMapper.readValue(body, EcsTask.class);
                                            } catch (JsonProcessingException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }))
                .body();
    }
}
