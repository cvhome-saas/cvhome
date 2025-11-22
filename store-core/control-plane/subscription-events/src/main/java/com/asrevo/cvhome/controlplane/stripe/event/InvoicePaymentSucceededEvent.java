package com.asrevo.cvhome.controlplane.stripe.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.subscription.commons.PriceId;
import java.time.Instant;
import java.util.Map;

public record InvoicePaymentSucceededEvent(ManagerOrgId org, PriceId priceId, Instant startDate, Instant endDate,
		Map<String, String> data) implements StripeEvent {

	public static InvoicePaymentSucceededEvent from(ManagerOrgId org, PriceId priceId, Instant startDate,
			Instant endDate) {
		return new InvoicePaymentSucceededEvent(org, priceId, startDate, endDate, Map.of());
	}

	@Override
	public Map<String, String> data() {
		return Map.of();
	}

	public String eventType() {
		return InvoicePaymentSucceededEvent.class.getSimpleName();
	}
}
