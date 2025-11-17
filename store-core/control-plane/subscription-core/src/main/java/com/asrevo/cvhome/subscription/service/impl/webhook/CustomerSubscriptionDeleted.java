package com.asrevo.cvhome.subscription.service.impl.webhook;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.EventProcessor;
import com.asrevo.cvhome.stripe.event.CustomerSubscriptionDeletedEvent;
import com.asrevo.cvhome.subscription.service.WebhookHandler;
import com.asrevo.cvhome.subscription.utils.ToJsonObj;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stripe.model.Event;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerSubscriptionDeleted implements WebhookHandler {

	private final ToJsonObj toJsonObj = new ToJsonObj();

	private final EventProcessor eventProcessor;

	@Override
	public void handle(Event event) {

		JsonObject jo = toJsonObj(event);

		JsonElement orgIdElement = jo.getAsJsonObject("metadata").get("orgId");

		ManagerOrgId orgId = new ManagerOrgId(orgIdElement.getAsString());

		eventProcessor.process(CustomerSubscriptionDeletedEvent.from(orgId));
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
