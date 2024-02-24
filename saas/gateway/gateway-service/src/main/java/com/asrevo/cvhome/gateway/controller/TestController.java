package com.asrevo.cvhome.gateway.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("api/v1/test")
@Slf4j
@AllArgsConstructor
public class TestController {
    private final HttpClient client = HttpClient.newHttpClient();

    @PostMapping("call")
    public String call(@RequestBody Request request) throws URISyntaxException, IOException, InterruptedException {
        log.info("this is ecs url {}", System.getenv("ECS_CONTAINER_METADATA_URI_V4"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(request.url()))
                .GET()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }
}

