package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class EcsTaskFetcher {

	private static final String ecsContainerMetadataUri = System.getenv("ECS_CONTAINER_METADATA_URI_V4");

	private static final HttpClient client = HttpClient.newHttpClient();

	private static final ObjectMapper objectMapper = getObjectMapper();

	private static ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	public static EcsTask fetch() {
		try {
			HttpRequest req = HttpRequest.newBuilder().uri(new URI(ecsContainerMetadataUri + "/task")).GET().build();
			return client
				.send(req, responseInfo -> HttpResponse.BodySubscribers
					.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), EcsTaskFetcher::getEcsTask))
				.body();
		}
		catch (IOException | InterruptedException | URISyntaxException e) {
			return new EcsTask();
		}
	}

	private static EcsTask getEcsTask(String body) {
		try {
			return objectMapper.readValue(body, EcsTask.class);
		}
		catch (JsonProcessingException e) {
			return new EcsTask();
		}
	}

}
