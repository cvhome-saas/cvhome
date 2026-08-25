package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reading Stripe's raw event JSON, and {@link ProviderSubscriptionState} on top of it.
 *
 * <p>
 * The document is read rather than the SDK's model objects on purpose: the SDK's types are bound to the API version
 * the library was built against, while a webhook arrives in whatever version the account is pinned to, and fields
 * have moved between the two. Every accessor therefore has to tolerate absence and answer null — a handler that
 * exploded on a missing optional field would fail an event Stripe then redelivers forever.
 * </p>
 */
class StripeJsonTest {

    private static final String ABSENT = "absent";

    private static final String ACTIVE = "active";

    private static final String ID = "id";

    private static final String INCOMPLETE = "incomplete";

    private static final String ITEMS = "items";

    private static final String NOTHING = "nothing";

    private static final String PAST_DUE = "past_due";

    private static final String SUB_1 = "sub_1";

    private static final String DOCUMENT = """
            {"id":"sub_1","status":"active","cancel_at_period_end":true,"amount_due":3000,
             "nothing":null,
             "metadata":{"storeId":"65f023632bc46470c104b76f"},
             "current_period_end":1769904000,
             "items":{"object":"list","data":[{"id":"si_1","price":{"id":"price_1"}}]},
             "empty":{"object":"list","data":[]},
             "notAList":{"data":"nope"},
             "scalar":"text"}""";

    private static JsonObject doc() {
        return StripeJson.parse(DOCUMENT);
    }

    @Test
    @DisplayName("strings, numbers and flags are read, and absence answers null or false")
    void readsScalars() {
        JsonObject doc = doc();

        assertThat(StripeJson.string(doc, ID)).isEqualTo(SUB_1);
        assertThat(StripeJson.number(doc, "amount_due")).isEqualTo(3000L);
        assertThat(StripeJson.flag(doc, "cancel_at_period_end")).isTrue();

        assertThat(StripeJson.string(doc, ABSENT)).isNull();
        assertThat(StripeJson.number(doc, ABSENT)).isNull();
        assertThat(StripeJson.flag(doc, ABSENT)).isFalse();
    }

    @Test
    @DisplayName("an explicit JSON null reads the same as an absent field")
    void jsonNullIsAbsence() {
        JsonObject doc = doc();

        // Stripe sends explicit nulls for unset optional fields, so the two have to be the same case.
        assertThat(StripeJson.string(doc, NOTHING)).isNull();
        assertThat(StripeJson.number(doc, NOTHING)).isNull();
        assertThat(StripeJson.flag(doc, NOTHING)).isFalse();
        assertThat(StripeJson.object(doc, NOTHING)).isNull();
    }

    @Test
    @DisplayName("reading from a null object is null rather than an NPE")
    void aNullObjectIsTolerated() {
        // The nested readers chain — object(object(x, a), b) — so the inner miss has to survive the outer call.
        assertThat(StripeJson.string(null, ID)).isNull();
        assertThat(StripeJson.number(null, ID)).isNull();
        assertThat(StripeJson.flag(null, ID)).isFalse();
        assertThat(StripeJson.object(null, ID)).isNull();
        assertThat(StripeJson.firstOfData(null, ITEMS)).isNull();
        assertThat(StripeJson.timestamp(null, ID)).isNull();
    }

    @Test
    @DisplayName("a field that is not an object reads as no object")
    void aScalarIsNotAnObject() {
        assertThat(StripeJson.object(doc(), "scalar")).isNull();
    }

    @Test
    @DisplayName("the first element of a nested data array is returned")
    void readsTheFirstOfData() {
        assertThat(StripeJson.string(StripeJson.firstOfData(doc(), ITEMS), ID)).isEqualTo("si_1");
    }

    @Test
    @DisplayName("an empty, missing or malformed data array answers nothing")
    void toleratesAnAbsentDataArray() {
        assertThat(StripeJson.firstOfData(doc(), "empty")).isNull();
        assertThat(StripeJson.firstOfData(doc(), ABSENT)).isNull();
        assertThat(StripeJson.firstOfData(doc(), "notAList")).isNull();
    }

    @Test
    @DisplayName("Stripe's epoch seconds become an Instant, and an unset one stays null")
    void readsTimestamps() {
        assertThat(StripeJson.timestamp(doc(), "current_period_end"))
                .isEqualTo(Instant.ofEpochSecond(1769904000L));
        assertThat(StripeJson.timestamp(doc(), ABSENT)).isNull();
    }

    @Test
    @DisplayName("a subscription's state is projected from the fields that drive a local transition")
    void projectsTheSubscriptionState() {
        ProviderSubscriptionState state = ProviderSubscriptionState.from(doc());

        assertThat(state.subscriptionId()).isEqualTo(SUB_1);
        assertThat(state.status()).isEqualTo(ACTIVE);
        assertThat(state.priceId()).isEqualTo("price_1");
        assertThat(state.cancelAtPeriodEnd()).isTrue();
        assertThat(state.currentPeriodEnd()).isEqualTo(Instant.ofEpochSecond(1769904000L));
    }

    @Test
    @DisplayName("the period is taken from the item when the subscription does not carry it")
    void readsThePeriodFromTheItem() {
        JsonObject doc = StripeJson.parse("""
                {"id":"sub_1","status":"active",
                 "items":{"data":[{"current_period_start":1767225600,"current_period_end":1769904000}]}}""");

        ProviderSubscriptionState state = ProviderSubscriptionState.from(doc);

        // Stripe moved these onto items in its 2025 API versions and an account can be pinned to either; reading
        // both is what lets one build serve accounts on both instead of recording a null renewal date.
        assertThat(state.currentPeriodStart()).isEqualTo(Instant.ofEpochSecond(1767225600L));
        assertThat(state.currentPeriodEnd()).isEqualTo(Instant.ofEpochSecond(1769904000L));
    }

    @Test
    @DisplayName("the subscription's own period wins over the item's when both are present")
    void theSubscriptionPeriodWins() {
        JsonObject doc = StripeJson.parse("""
                {"id":"sub_1","current_period_end":100,
                 "items":{"data":[{"current_period_end":200}]}}""");

        assertThat(ProviderSubscriptionState.from(doc).currentPeriodEnd()).isEqualTo(Instant.ofEpochSecond(100L));
    }

    @Test
    @DisplayName("Stripe's status strings are grouped into the three questions that matter")
    void classifiesStatus() {
        assertThat(state(ACTIVE).paying()).isTrue();
        assertThat(state("trialing").paying()).isTrue();
        assertThat(state(PAST_DUE).paying()).isFalse();

        assertThat(state(PAST_DUE).pastDue()).isTrue();
        assertThat(state("unpaid").pastDue()).isTrue();
        assertThat(state(ACTIVE).pastDue()).isFalse();

        assertThat(state("canceled").ended()).isTrue();
        assertThat(state("incomplete_expired").ended()).isTrue();
        assertThat(state(ACTIVE).ended()).isFalse();

        // A status none of the three recognise — an unfamiliar one, or none at all — is not any of them, so a
        // reconciliation leaves the local row where it is rather than guessing.
        assertThat(state(INCOMPLETE).paying()).isFalse();
        assertThat(state(INCOMPLETE).pastDue()).isFalse();
        assertThat(state(INCOMPLETE).ended()).isFalse();
        assertThat(ProviderSubscriptionState.from(StripeJson.parse("{}")).paying()).isFalse();
    }

    private static ProviderSubscriptionState state(String status) {
        return ProviderSubscriptionState.from(StripeJson.parse(String.format("{\"status\":\"%s\"}", status)));
    }

}
