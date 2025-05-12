package com.asrevo.cvhome.subscription.service.impl.webhook;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.EventProcessor;
import com.asrevo.cvhome.stripe.event.InvoicePaymentSucceededEvent;
import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.service.WebhookHandler;
import com.asrevo.cvhome.subscription.utils.ToJsonObj;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stripe.model.Event;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class InvoicePaymentSucceeded implements WebhookHandler {
    private final ToJsonObj toJsonObj = new ToJsonObj();
    private final EventProcessor eventProcessor;

    @Override
    public void handle(Event event) {

        JsonObject jo = toJsonObj(event);

        JsonObject data = jo.getAsJsonObject("lines").getAsJsonArray("data").get(0).getAsJsonObject();
        JsonElement orgIdElement = jo.getAsJsonObject("parent").getAsJsonObject("subscription_details").getAsJsonObject("metadata").get("orgId");
        JsonElement priceIdElement = data.getAsJsonObject("pricing").getAsJsonObject("price_details").get("price");
        JsonElement startElement = data.getAsJsonObject("period").get("start");
        JsonElement endElement = data.getAsJsonObject("period").get("end");

        ManagerOrgId orgId = new ManagerOrgId(orgIdElement.getAsString());
        PriceId priceId = new PriceId(priceIdElement.getAsString());
        Instant startDate = Instant.ofEpochSecond(startElement.getAsLong());
        Instant endDate = Instant.ofEpochSecond(endElement.getAsLong());
        eventProcessor.process(InvoicePaymentSucceededEvent.from(orgId, priceId, startDate, endDate));
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

