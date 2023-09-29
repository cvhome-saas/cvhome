package com.asrevo.cvhome.domainownership.service;

import com.asrevo.cvhome.commons.command.Command;
import com.asrevo.cvhome.commons.command.CommandProcessor;
import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
@AllArgsConstructor
public class ExternalEventListener {
    private final CommandProcessor commandProcessor;
    private final EventProcessor eventProcessor;
    /*
     *
     * GenericMessage [payload={@class=com.github.aznamier.keycloak.event.provider.EventAdminNotificationMqMsg, time=1694174044731, realmId=c4e1ca23-4168-4488-88b3-dc009dea7b85, authDetails={realmId=ddcdb09c-3f54-47fe-916d-3621181a312b, clientId=b99b1b58-7b4b-4757-a9ac-09d4f08987ab, userId=04e3d577-2f4b-4054-96c2-e9702bb7213a, ipAddress=172.28.0.1}, resourceType=USER, operationType=CREATE, resourcePath=users/4618f1b8-b4d0-44e3-bb15-635aee6ca738, representation={"username":"ssssssssss","enabled":true,"emailVerified":false,"firstName":"ssssssssss","lastName":"sssssss","email":"","requiredActions":[],"groups":[]}, resourceTypeAsString=USER}, headers={amqp_receivedExchange=com.asrevo.cvhome.sys-events, amqp_deliveryTag=1, deliveryAttempt=1, amqp_consumerQueue=com.asrevo.cvhome.sys-events.certificate-manager, amqp_redelivered=false, amqp_receivedRoutingKey=KK.EVENT.ADMIN.c4e1ca23-4168-4488-88b3-dc009dea7b85.SUCCESS.USER.CREATE, amqp_contentEncoding=UTF-8, amqp_appId=Keycloak, json__TypeId__=com.github.aznamier.keycloak.event.provider.EventAdminNotificationMqMsg, id=ebd5ed0e-4555-2c3e-1644-9f483405df21, amqp_consumerTag=amq.ctag-FAiTt-AXxGPvyMdM66vzSw, sourceData=(Body:'[B@520a0894(byte[752])' MessageProperties [headers={__TypeId__=com.github.aznamier.keycloak.event.provider.EventAdminNotificationMqMsg}, appId=Keycloak, contentType=application/json, contentEncoding=UTF-8, contentLength=0, redelivered=false, receivedExchange=com.asrevo.cvhome.sys-events, receivedRoutingKey=KK.EVENT.ADMIN.c4e1ca23-4168-4488-88b3-dc009dea7b85.SUCCESS.USER.CREATE, deliveryTag=1, consumerTag=amq.ctag-FAiTt-AXxGPvyMdM66vzSw, consumerQueue=com.asrevo.cvhome.sys-events.certificate-manager]), contentType=application/json, __TypeId__=com.github.aznamier.keycloak.event.provider.EventAdminNotificationMqMsg, timestamp=1694174044758}]
     *
     * */

    @Bean
    public Consumer<Message<String>> inSysEvents() {
        return event -> {
            try {
                System.out.println("Received: sys event: " + event.getPayload());
            } catch (Exception e) {
                log.error("error processing " + event.getPayload());
            }
        };
    }

    @Bean
    public Consumer<Message<Event>> inOrderEvents() {
        return event -> {
            System.out.println("Received: order event: " + event.getPayload().eventType());
            try {
                eventProcessor.process(event.getPayload());
            } catch (Exception e) {
                log.error("error processing " + event.getPayload());
            }
        };
    }

    @Bean
    public Consumer<Message<Command>> inOrderCommands() {
        return event -> {
            System.out.println("Received: order command: " + event.getPayload());
            try {
                commandProcessor.process(event.getPayload());
            } catch (Exception e) {
                log.error("error processing " + event.getPayload());
            }
        };
    }
}
