package com.asrevo.cvhome.controlplane.subscription.service.impl.webhook;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.stripe.event.InvoicePaymentSucceededEvent;
import com.asrevo.cvhome.controlplane.subscription.commons.PriceId;
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
public class InvoicePaymentSucceeded implements WebhookHandler {

    private final ToJsonObj toJsonObj = new ToJsonObj();

    private final Outbox outbox;

    @Override
    public void handle(Event event) {

        JsonObject jo = toJsonObj(event);

        JsonObject data = jo.getAsJsonObject("lines").getAsJsonArray("data").get(0).getAsJsonObject();
        JsonElement orgIdElement = jo.getAsJsonObject("parent")
                .getAsJsonObject("subscription_details")
                .getAsJsonObject("metadata")
                .get("orgId");
        JsonElement priceIdElement = data.getAsJsonObject("pricing").getAsJsonObject("price_details").get("price");
        JsonObject period = data.getAsJsonObject("period");
        JsonElement startElement = period.get("start");
        JsonElement endElement = period.get("end");

        ManagerOrgId orgId = new ManagerOrgId(orgIdElement.getAsString());
        PriceId priceId = new PriceId(priceIdElement.getAsString());
        Instant startDate = Instant.ofEpochSecond(startElement.getAsLong());
        Instant endDate = Instant.ofEpochSecond(endElement.getAsLong());
        var e = InvoicePaymentSucceededEvent.from(orgId, priceId, startDate, endDate);
        outbox.schedule(e, e.org().getId().toString());
        log.info("Invoice payment succeeded for {}  {} start {} end {}", orgId, priceId, startDate, endDate);
    }

    @Override
    public String type() {
        return "invoice.payment_succeeded";
    }

    private JsonObject toJsonObj(Event event) {
        return toJsonObj.exec(event.getDataObjectDeserializer().getRawJson());
    }

}
