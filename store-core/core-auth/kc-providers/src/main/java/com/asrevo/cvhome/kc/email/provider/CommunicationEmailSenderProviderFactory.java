package com.asrevo.cvhome.kc.email.provider;

import com.asrevo.cvhome.kc.service.CommunicationService;
import com.asrevo.cvhome.kc.service.impl.CommunicationServiceImpl;
import java.util.HashMap;
import java.util.Map;
import org.keycloak.Config;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

public class CommunicationEmailSenderProviderFactory
        implements EmailSenderProviderFactory, ServerInfoAwareProviderFactory {

    private final Map<String, String> configMap = new HashMap<>();
    private CommunicationService communicationService;
    private CommunicationEmailSenderProvider communicationEmailSenderProvider;

    @Override
    public EmailSenderProvider create(KeycloakSession session) {
        this.communicationEmailSenderProvider =
                new CommunicationEmailSenderProvider(session, communicationService);
        return this.communicationEmailSenderProvider;
    }

    @Override
    public void init(Config.Scope config) {
        communicationService = new CommunicationServiceImpl();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        return configMap;
    }
}
