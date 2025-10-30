package com.asrevo.cvhome.subscription.service;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.ProductId;
import com.asrevo.cvhome.subscription.service.impl.ProductPriceDetails;
import java.util.List;

public interface StripeInitService {

	boolean isConfigured();

	ProductId createProduct(SubscriptionPlan plan);

	PriceId createProductPrice(ProductPriceDetails details);

	boolean exist(PriceId priceId);

	List<ProductPriceDetails> loadTable();

}
