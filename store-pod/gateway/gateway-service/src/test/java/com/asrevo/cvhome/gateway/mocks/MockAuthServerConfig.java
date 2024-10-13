package com.asrevo.cvhome.gateway.mocks;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@ConditionalOnProperty(prefix = "mock", name = "auth.enabled", matchIfMissing = false)
public class MockAuthServerConfig {

    @Bean
    public MockServerContainer authMockServer() {
        DockerImageName MOCKSERVER_IMAGE =
                DockerImageName.parse("mockserver/mockserver")
                        .withTag(
                                "mockserver-"
                                        + MockServerClient.class
                                                .getPackage()
                                                .getImplementationVersion());
        MockServerContainer container = new MockServerContainer(MOCKSERVER_IMAGE);
        container.setPortBindings(List.of("9999:1080"));
        return container;
    }

    @Bean
    public CommandLineRunner authRunner(
            @Qualifier("authMockServer") MockServerContainer authMockServer) {
        return args -> {
            MockServerClient mockServerClient =
                    new MockServerClient(authMockServer.getHost(), authMockServer.getServerPort());
            mockServerClient
                    .when(
                            request()
                                    //            .withMethod("POST")
                                    .withPath("/realms/cvhome/protocol/openid-connect/token")
                                    //            .withContentType(new
                                    // MediaType("application","x-www-form-urlencoded","UTF-8",null))
                                    //
                                    // .withBody("client_id=microservice-app&client_secret=r1nuVn4HefusLGwHIeRxbdOzdTZLEMvQ&grant_type=password&username=microservice-gateway&password=microservice-gateway")
                                    .withBody(
                                            "grant_type=password&username=microservice-gateway&password=microservice-gateway"))
                    .respond(
                            response()
                                    .withContentType(MediaType.APPLICATION_JSON)
                                    .withBody(
                                            """
                                            {
                                              "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJRZmpUVUItMnppUW5pNmR5VVAzaGh6RXQ1N1U4UWExbUhPLV9xRDloNWdBIn0.eyJleHAiOjE3MDg5NjQwOTksImlhdCI6MTcwODk2Mzc5OSwianRpIjoiMDQ0Mzg5NTQtZDNhYy00ZjcwLTgyZTQtNjMxYmUzOGI4MGI4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5OTk5L3JlYWxtcy9jdmhvbWUiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiZmNhZDk0MWMtYWVhNS00MjZmLThmMTctNDI4YWRkODJhZWIxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibWljcm9zZXJ2aWNlLWFwcCIsInNlc3Npb25fc3RhdGUiOiIyYmI3OTRmMS1kMmVmLTQ1MjItYTk1My1mM2U2M2Q5YmNhYzIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIi8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJST0xFX01JQ1JPU0VSVklDRSIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWN2aG9tZSIsIlJPTEVfTUlDUk9TRVJWSUNFX0dBVEVXQVkiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIyYmI3OTRmMS1kMmVmLTQ1MjItYTk1My1mM2U2M2Q5YmNhYzIiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6Im1pY3Jvc2VydmljZS1nYXRld2F5IiwiZ2l2ZW5fbmFtZSI6IiIsImZhbWlseV9uYW1lIjoiIn0.pwVXZS5RYWX-GhmcFs3O0pPqj92yu4K6-fgO7zZIbjWpHmxdNX5eoWlKBcfi3Fr8kJ2OTPkaTY1aZ2CgpUGbAcagdEf9A3e2ZK0GkBzyCa54d0ywtDTdvLaWVBz8UYqdtipsOvrs2FnZnG1nZP3wlHIwn5XSaZbhnid-Cjo3rD1-CAb-upnsh7nnW6g7WTfH6W5aM-V_nu7p9BfxstypGEi8QuoK5OZU8AtVlZ9J2x8lYSPyg3VVgu8_lln41kLJmN7MDTro8aF2fg15XBqk4JKleLN53LRjXkZgO9f98-yzmxZeodfgagcH6s8g7hjxYGHeGTxpUIRgzXnzDgfIMw",
                                              "expires_in": 300,
                                              "refresh_expires_in": 1800,
                                              "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJhOTdjZDE5Yi00MGM3LTQ2MDUtOWIzMi02Yjc0OGRjZmY1MjgifQ.eyJleHAiOjE3MDg5NjU1OTksImlhdCI6MTcwODk2Mzc5OSwianRpIjoiZmY5NDIwYmMtYWFhYy00YmMyLTg0Y2QtMDI2NzE4YTdhMDk1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5OTk5L3JlYWxtcy9jdmhvbWUiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0Ojk5OTkvcmVhbG1zL2N2aG9tZSIsInN1YiI6ImZjYWQ5NDFjLWFlYTUtNDI2Zi04ZjE3LTQyOGFkZDgyYWViMSIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJtaWNyb3NlcnZpY2UtYXBwIiwic2Vzc2lvbl9zdGF0ZSI6IjJiYjc5NGYxLWQyZWYtNDUyMi1hOTUzLWYzZTYzZDliY2FjMiIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjJiYjc5NGYxLWQyZWYtNDUyMi1hOTUzLWYzZTYzZDliY2FjMiJ9.OE0fzfrNQpEEAY1s4W-m4Gu8p9TDetSg5ow373iu5jQ",
                                              "token_type": "Bearer",
                                              "not-before-policy": 0,
                                              "session_state": "2bb794f1-d2ef-4522-a953-f3e63d9bcac2",
                                              "scope": "profile email"
                                            }
                                            """));
        };
    }
}
