package com.asrevo.cvhome.kc.email.provider;

import com.asrevo.cvhome.kc.service.CommunicationService;
import com.asrevo.cvhome.kc.utils.AccessTokenGenerator;
import java.util.Map;
import org.jboss.logging.Logger;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

public class CommunicationEmailSenderProvider implements EmailSenderProvider {
    private static final org.jboss.logging.Logger log =
            Logger.getLogger(CommunicationEmailSenderProvider.class);
    private static final String realmName = "cvhome";
    private final CommunicationService communicationService;
    private final AccessTokenGenerator tokenGenerator;
    private String accessToken = null;

    CommunicationEmailSenderProvider(
            KeycloakSession session, CommunicationService communicationService) {
        this.communicationService = communicationService;
        this.tokenGenerator = new AccessTokenGenerator(session, realmName);
        this.communicationService.setTokenInterceptor(buildTokenInterceptor());
    }

    private TokenInterceptor buildTokenInterceptor() {
        return new TokenInterceptor() {
            @Override
            public String getAccessToken() {
                if (accessToken == null) {
                    CommunicationEmailSenderProvider.this.accessToken =
                            CommunicationEmailSenderProvider.this.tokenGenerator.generate();
                }
                return accessToken;
            }
        };
    }

    @Override
    public void send(
            Map<String, String> config,
            UserModel user,
            String subject,
            String textBody,
            String htmlBody) {
        this.send(config, user.getEmail(), subject, textBody, htmlBody);
    }

    @Override
    public void send(
            Map<String, String> config,
            String address,
            String subject,
            String textBody,
            String htmlBody) {
        log.info("Sending **********email********** with textBody: " + textBody);
        this.communicationService.sendMessage(address, textBody);
    }

    @Override
    public void close() {}
}
