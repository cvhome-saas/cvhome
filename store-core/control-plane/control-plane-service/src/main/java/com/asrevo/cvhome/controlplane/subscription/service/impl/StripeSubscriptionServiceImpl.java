package com.asrevo.cvhome.controlplane.subscription.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.s2s.model.StripeProperties;
import com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder;
import com.asrevo.cvhome.controlplane.subscription.commons.PriceId;
import com.asrevo.cvhome.controlplane.subscription.service.StripeSubscriptionService;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class StripeSubscriptionServiceImpl implements StripeSubscriptionService {

	private final StripeProperties stripeProperties;

	@PostConstruct
	public void init() {
		Stripe.apiKey = stripeProperties.key();
	}

	@SneakyThrows
	private Customer findOrCreateCustomer(ManagerOrgId managerOrgId) {
		CustomerSearchParams params = CustomerSearchParams.builder()
			.setQuery("metadata[\"orgId\"]:\"" + managerOrgId.id().toString() + "\"")
			.build();

		CustomerSearchResult search = Customer.search(params);
		Customer customer;
		if (search.getData().isEmpty()) {

			CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
				.putMetadata("orgId", managerOrgId.id().toString())
				.build();
			customer = Customer.create(customerCreateParams);
		}
		else {
			customer = search.getData().getFirst();
		}

		return customer;
	}

	@Override
	@SneakyThrows
	public String createSubscriptionSession(ManagerOrgId managerOrgId, PriceId priceId,
			RedirectionUrlBuilder redirectionUrl) {
		Customer customer = findOrCreateCustomer(managerOrgId);

		SessionCreateParams.Builder sessionCreateParamsBuilder = SessionCreateParams.builder()
			.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
			.setCustomer(customer.getId())
			.setSuccessUrl(redirectionUrl.getRedirectionUrl("/public/subscription/success"))
			.setCancelUrl(redirectionUrl.getRedirectionUrl("/public/subscription/fail"))
			.addLineItem(SessionCreateParams.LineItem.builder().setQuantity(1L).setPrice(priceId.id()).build())
			.setSubscriptionData(SessionCreateParams.SubscriptionData.builder()
				.putMetadata("orgId", managerOrgId.id().toString())
				.build());

		Session session = Session.create(sessionCreateParamsBuilder.build());
		return session.getUrl();
	}

}
