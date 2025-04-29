package com.asrevo.cvhome.subscription.service.impl;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.s2s.model.StripeProperties;
import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.subscription.commons.ProductId;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;
import com.asrevo.cvhome.subscription.service.StripeInitService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceCreateParams.Recurring.Interval;
import com.stripe.param.ProductCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class StripeInitServiceImpl implements StripeInitService {
    private final StripeProperties stripeProperties;

    private static Interval getInterval(RecurringPlan recurringPlan) {
        return switch (recurringPlan) {
            case MONTHLY -> Interval.MONTH;
            case YEARLY -> Interval.YEAR;
        };
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.key();
    }

    @SneakyThrows
    @Override
    public ProductId createProduct(SubscriptionPlan plan) {
        String productName = plan.name();
        ProductCreateParams productParams =
                ProductCreateParams.builder().setName(productName)
                        .setDescription(productName + " Plan")
                        .build();
        Product product = Product.create(productParams);
        return new ProductId(product.getId());
    }

    @SneakyThrows
    @Override
    public PriceId createPrice(ProductId productId, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan, PricePlanCost pricePlanCost) {
        String productName = subscriptionPlan.name();
        PriceCreateParams priceParams =
                PriceCreateParams.builder()
                        .setNickname(productName + " " + recurringPlan.name())
                        .setCurrency(pricePlanCost.currency())
                        .setUnitAmount(pricePlanCost.price())
                        .setRecurring(
                                PriceCreateParams.Recurring.builder()
                                        .setInterval(getInterval(recurringPlan))
                                        .build()
                        )
                        .setProduct(productId.productId())
                        .build();
        Price price = Price.create(priceParams);
        return new PriceId(price.getId());
    }

    @Override
    public boolean exist(PriceId priceId) {
        try {
            Price ignored = Price.retrieve(priceId.id());
            return true;
        } catch (StripeException e) {
            return false;
        }
    }
}
