package com.asrevo.cvhome.kc.event.provider;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;


public class LoggingEventListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(LoggingEventListenerProvider.class);


    @Override
    public void close() {

    }

    @Override
    public void onEvent(Event event) {
        log.info("********************* Received event: " + event);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        log.info("********************* Received admin event: " + adminEvent);
    }


}