package com.asrevo.cvhome.controlplane.subscription.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.subscription.commons.PriceId;
import com.asrevo.cvhome.controlplane.subscription.service.StripeSubscriptionService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder.getPort;
import static com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder.getScheme;

@Controller
@RequestMapping("api/v1/subscription")
@Slf4j
@AllArgsConstructor
public class SubscriptionController {

    private final StripeSubscriptionService stripeSubscriptionService;

    private final ServiceDomainProperties serviceDomainProperties;

    @GetMapping("subscribe")

    public ResponseEntity<Void> subscribe(ServerWebExchange exchange,
                                          @OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam PriceId priceId) {
        ServerHttpRequest request = exchange.getRequest();
        ServiceDomain serviceDomain = serviceDomainProperties.getService("seller-ui");
        RedirectionUrlBuilder urlBuilder = new RedirectionUrlBuilder(getScheme(request), getPort(request),
                serviceDomain);
        String sessionUrl = stripeSubscriptionService.createSubscriptionSession(identity.org(), priceId, urlBuilder);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", sessionUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
