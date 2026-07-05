package com.asrevo.cvhome.controlplane.subscription.service.impl.webhook;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.stripe.event.CustomerSubscriptionDeletedEvent;
import com.asrevo.cvhome.controlplane.subscription.service.WebhookHandler;
import com.asrevo.cvhome.controlplane.subscription.utils.ToJsonObj;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stripe.model.Event;

import io.namastack.outbox.Outbox;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerSubscriptionDeleted implements WebhookHandler {

    private final ToJsonObj toJsonObj = new ToJsonObj();

    private final Outbox outbox;

    @Override
    public void handle(Event event) {

        JsonObject jo = toJsonObj(event);

        JsonElement orgIdElement = jo.getAsJsonObject("metadata").get("orgId");

        ManagerOrgId orgId = new ManagerOrgId(orgIdElement.getAsString());

        var e = CustomerSubscriptionDeletedEvent.from(orgId);
        outbox.schedule(e, e.org().getId().toString());
        log.info("Customer subscription deleted for {}", orgId);
    }

    @Override
    public String type() {
        return "customer.subscription.deleted";
    }

    private JsonObject toJsonObj(Event event) {
        return toJsonObj.exec(event.getDataObjectDeserializer().getRawJson());
    }

}
