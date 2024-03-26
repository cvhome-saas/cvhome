package com.asrevo.cvhome.gateway.mocks;

import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Configuration
@ConditionalOnProperty(prefix = "mock", name = "dcm.enabled", matchIfMissing = false)
public class MockDomainCertificateManagerServerConfig {

    @Bean
    public MockServerContainer domainCertificateManagerMockServer() {
        DockerImageName MOCKSERVER_IMAGE = DockerImageName
                .parse("mockserver/mockserver")
                .withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion());
        MockServerContainer container = new MockServerContainer(MOCKSERVER_IMAGE);
        container.setPortBindings(List.of("8082:1080"));
        return container;
    }

    @Bean
    public CommandLineRunner domainCertificateManagerRunner(@Qualifier("domainCertificateManagerMockServer") MockServerContainer domainCertificateManagerMockServer) {
        return args -> {
            MockServerClient mockServerClient = new MockServerClient(domainCertificateManagerMockServer.getHost(), domainCertificateManagerMockServer.getServerPort());
            mockServerClient
                    .when(
                            request()
                                    .withMethod("POST")
                                    .withPath("/api/v1/domain-ownership/get-reference")
                                    .withBody(new JsonBody("""
                                               {
                                                  "domain":"ashraf.gateway.me",
                                               }
                                            """))
                    )
                    .respond(
                            response()
                                    .withContentType(MediaType.APPLICATION_JSON)
                                    .withBody("""
                                               {
                                                  "id": 1,
                                                  "domain":"ashraf.gateway.me",
                                                  "reference":"65db438ee842462c224e2fb2"
                                               }
                                            """
                                    ));
        };
    }
}
