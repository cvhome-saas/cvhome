package com.asrevo.cvhome.billing.service.stripe;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.stripe.StripeClient;
import com.stripe.exception.ApiConnectionException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * One Stripe customer per org, and the two ways that can go wrong.
 *
 * <p>
 * The lookup is deliberately against our own rows rather than Stripe's customer search — search is eventually
 * consistent, so two stores created for one org moments apart would both miss it and the org would end up with its
 * payment methods split across two customers.
 * </p>
 */
class StripeCustomerGatewayTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private StripeClient stripe;

    private StoreSubscriptionRepository subscriptions;

    private StripeRequestRepository requests;

    private StripeCustomerGateway gateway;

    @BeforeEach
    void setUp() {
        StripeCredentials credentials = mock(StripeCredentials.class);
        when(credentials.apiKey()).thenReturn("sk_test_customer");
        stripe = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        subscriptions = mock(StoreSubscriptionRepository.class);
        requests = mock(StripeRequestRepository.class);
        when(requests.existsById(anyString())).thenReturn(false);
        when(requests.findById(anyString())).thenReturn(Optional.empty());
        gateway = new StripeCustomerGateway(credentials, requests, stripe, subscriptions);
    }

    @Test
    @DisplayName("an org that already has a customer is not given a second one")
    void reusesTheOrgsCustomer() throws Exception {
        when(subscriptions.findCustomerOf(ORG)).thenReturn(Optional.of(new StripeCustomerId("cus_existing")));

        StripeCustomerId id = gateway.findOrCreate(ORG, "owner@example.test");

        assertThat(id).isEqualTo(new StripeCustomerId("cus_existing"));
        // Not one call to Stripe. Billing details, the card and the portal are things an org owns once; creating a
        // second customer would ask the owner to enter the same card for every store they open.
        verify(stripe.customers(), never()).create(any(CustomerCreateParams.class), any(RequestOptions.class));
    }

    @Test
    @DisplayName("the first store of an org mints a customer carrying the org id")
    void createsTheCustomer() throws Exception {
        when(subscriptions.findCustomerOf(ORG)).thenReturn(Optional.empty());
        Customer created = mock(Customer.class);
        when(created.getId()).thenReturn("cus_new");
        when(stripe.customers().create(any(CustomerCreateParams.class), any(RequestOptions.class)))
                .thenReturn(created);

        StripeCustomerId id = gateway.findOrCreate(ORG, "owner@example.test");

        ArgumentCaptor<CustomerCreateParams> params = ArgumentCaptor.forClass(CustomerCreateParams.class);
        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripe.customers()).create(params.capture(), options.capture());
        assertThat(id).isEqualTo(new StripeCustomerId("cus_new"));
        assertThat(params.getValue().getEmail()).isEqualTo("owner@example.test");
        // The org travels in metadata so a Stripe dashboard row can be traced back to a tenant.
        assertThat(params.getValue().getMetadata())
                .isEqualTo(java.util.Map.of("orgId", ORG.getId().toString()));
        // Derived from the org alone, with no time component: "this org's customer" is a fact that must never be
        // created twice however far apart the attempts are.
        assertThat(options.getValue().getIdempotencyKey()).isEqualTo("customer:" + ORG.getId());
    }

    @Test
    @DisplayName("a failure to reach Stripe leaves it unknown whether a customer now exists")
    void failureIsAlwaysUnavailable() throws Exception {
        when(subscriptions.findCustomerOf(ORG)).thenReturn(Optional.empty());
        when(stripe.customers().create(any(CustomerCreateParams.class), any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException("no route to stripe"));

        // Every failure of this call is an unknown outcome, never a refusal: creating a customer takes no payment,
        // so there is nothing for a card to decline.
        assertThatThrownBy(() -> gateway.findOrCreate(ORG, "owner@example.test"))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

}
