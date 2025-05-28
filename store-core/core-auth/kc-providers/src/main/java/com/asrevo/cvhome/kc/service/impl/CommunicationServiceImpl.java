package com.asrevo.cvhome.kc.service.impl;

import com.asrevo.cvhome.kc.email.provider.TokenInterceptor;
import com.asrevo.cvhome.kc.service.CommunicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

public class CommunicationServiceImpl implements CommunicationService {
    private static final org.jboss.logging.Logger log = Logger.getLogger(CommunicationServiceImpl.class);
    private static final String MAIL_SEND_PATH = "/api/v1/mail/send";
    private final URI sendMailOnCommunicationServiceUri;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String COMMUNICATION_SERVICE_ENDPOINT_ENV_KEY = "COMMUNICATION_SERVICE_ENDPOINT";
    private final String FALLBACK_URL = "http://localhost:8000/communication";
    private TokenInterceptor tokenInterceptor;

    @SneakyThrows
    public CommunicationServiceImpl() {
        String endpointEnvValue = System.getenv(COMMUNICATION_SERVICE_ENDPOINT_ENV_KEY);
        String communicationServiceEndpoint = Optional.ofNullable(endpointEnvValue).orElse(FALLBACK_URL);
        this.sendMailOnCommunicationServiceUri = new URI(communicationServiceEndpoint + MAIL_SEND_PATH);
    }

    @Override
    public void sendMessage(String address, String htmlBody) {

        if (log.isInfoEnabled()) {
            log.info("Sending JSON-MAIL message to " + address + " and text body " + htmlBody);
        }
        this.call(address, htmlBody);
    }

    @Override
    public void setTokenInterceptor(TokenInterceptor tokenInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
    }

    private void call(String address, String textBody) {
        try {

            String accessToken = this.tokenInterceptor.getAccessToken();
            log.info("Retrieving access token " + accessToken + " to call communication service");
            Map<String, Object> payloadMap = Map.of("body", ":_TEXT_BODY", "userEmail", Map.of("email", address));

            String payloadStr = mapper.writeValueAsString(payloadMap);

            payloadStr = payloadStr.replace("\":_TEXT_BODY\"", textBody);

            log.info("will send mail with this payload " + payloadStr + " on this endpoint " + this.sendMailOnCommunicationServiceUri);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(sendMailOnCommunicationServiceUri)
                    .header("Content-Type", "application/json")
                    .headers("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(payloadStr))
                    .build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("sent mail with response status " + response.statusCode());
        } catch (JsonProcessingException e) {
            log.error("Error while processing mail payload", e);
        } catch (IOException e) {
            log.error("Error while calling communication service", e);
        } catch (InterruptedException e) {
            log.error("Error while calling communication service", e);
            Thread.currentThread().interrupt();
        }
    }

}
