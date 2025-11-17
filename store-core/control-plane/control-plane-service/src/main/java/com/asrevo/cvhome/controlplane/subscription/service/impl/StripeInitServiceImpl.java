package com.asrevo.cvhome.controlplane.subscription.service.impl;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.s2s.model.StripeProperties;
import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.subscription.commons.ProductId;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;
import com.asrevo.cvhome.controlplane.subscription.service.StripeInitService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceCreateParams.Recurring.Interval;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductListParams;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class StripeInitServiceImpl implements StripeInitService {

	private final StripeProperties stripeProperties;

	@PostConstruct
	public void init() {
		Stripe.apiKey = stripeProperties.key();
	}

	@Override
	public boolean isConfigured() {
		try {
			Product.list(ProductListParams.builder().build());
			return true;
		}
		catch (StripeException e) {
			return false;
		}
	}

	@SneakyThrows
	@Override
	public ProductId createProduct(SubscriptionPlan plan) {
		String productName = plan.name();
		ProductCreateParams productParams = ProductCreateParams.builder()
			.setName(productName)
			.setDescription(productName + " Plan")
			.build();
		Product product = Product.create(productParams);
		return new ProductId(product.getId());
	}

	@SneakyThrows
	@Override
	public PriceId createProductPrice(ProductPriceDetails details) {
		String productName = details.subscriptionPlan().name();
		PriceCreateParams priceParams = PriceCreateParams.builder()
			.setNickname(productName + " " + details.recurringPlan().name())
			.setCurrency(details.pricePlanCost().currency())
			.setUnitAmount(details.pricePlanCost().price())
			.setRecurring(PriceCreateParams.Recurring.builder()
				.setInterval(Interval.valueOf(details.recurringPlan().name()))
				.build())
			.setProduct(details.productId().productId())
			.build();
		Price price = Price.create(priceParams);
		return new PriceId(price.getId());
	}

	@Override
	public boolean exist(PriceId priceId) {
		try {
			Price ignored = Price.retrieve(priceId.id());
			return true;
		}
		catch (StripeException e) {
			return false;
		}
	}

	@SneakyThrows
	@Override
	public List<ProductPriceDetails> loadTable() {
		List<ProductPriceDetails> productPriceDetails = new ArrayList<>();

		ProductCollection list = Product.list(ProductListParams.builder().setActive(true).build());
		for (Product product : list.getData()) {

			List<Price> prices = Price.list(PriceListParams.builder().setProduct(product.getId()).build()).getData();
			for (Price price : prices) {
				try {
					ProductId productId = new ProductId(product.getId());
					SubscriptionPlan subscriptionPlan = SubscriptionPlan.valueOf(product.getName().toUpperCase());
					RecurringPlan recurringPlan = RecurringPlan
						.valueOf(price.getRecurring().getInterval().toUpperCase());
					PricePlanCost pricePlanCost = new PricePlanCost(price.getCurrency(), price.getUnitAmount());

					PricePlanCost expectedPriceCost = PricePlanCost.fromUsingFactor(price.getCurrency(),
							subscriptionPlan, recurringPlan);

					if (expectedPriceCost.equals(pricePlanCost)) {
						ProductPriceDetails e = new ProductPriceDetails(productId, subscriptionPlan, recurringPlan,
								pricePlanCost);
						productPriceDetails.add(e);
					}
					else {
						log.warn("ignore price  {} not matched {}", pricePlanCost, expectedPriceCost);
					}

				}
				catch (Exception e) {
					log.warn("ignore product price for failing with error ", e);
				}
			}
		}
		return productPriceDetails;
	}

}
