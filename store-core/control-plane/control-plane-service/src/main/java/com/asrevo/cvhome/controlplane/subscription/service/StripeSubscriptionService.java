package com.asrevo.cvhome.controlplane.subscription.service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder;
import com.asrevo.cvhome.subscription.commons.PriceId;

public interface StripeSubscriptionService {

	String createSubscriptionSession(ManagerOrgId managerOrgId, PriceId priceId, RedirectionUrlBuilder redirectionUrl);

}
