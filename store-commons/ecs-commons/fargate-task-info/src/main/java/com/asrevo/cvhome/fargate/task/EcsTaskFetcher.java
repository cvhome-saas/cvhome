package com.asrevo.cvhome.fargate.task;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

public final class EcsTaskFetcher {

    private static final String ECS_CONTAINER_METADATA_URI = System.getenv("ECS_CONTAINER_METADATA_URI_V4");

    private static final HttpClient client = HttpClient.newHttpClient();

    private static final ObjectMapper objectMapper = getObjectMapper();

    private EcsTaskFetcher() {
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static EcsTask fetch() {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(new URI(String.format("%s/task", ECS_CONTAINER_METADATA_URI))).GET().build();
            return client
                    .send(req, responseInfo -> HttpResponse.BodySubscribers
                            .mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), EcsTaskFetcher::getEcsTask))
                    .body();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return new EcsTask();
        } catch (IOException | URISyntaxException _) {
            return new EcsTask();
        }
    }

    private static EcsTask getEcsTask(String body) {
        return objectMapper.readValue(body, EcsTask.class);
    }

}
