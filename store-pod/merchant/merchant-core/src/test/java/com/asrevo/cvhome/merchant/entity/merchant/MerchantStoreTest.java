package com.asrevo.cvhome.merchant.entity.merchant;

import java.time.LocalDate;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.asrevo.cvhome.cache.event.StoreChanged;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.model.references.MeasureUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The defaults a freshly created store row carries, which the schema's NOT NULL columns and the readable populator's
 * {@code valueOf} calls both rely on.
 */
class MerchantStoreTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String NAME = "Shop";

    private static final String EMAIL = "shop@example.com";

    /** {@code AbstractAggregateRoot.domainEvents()} is protected; Spring Data reads it reflectively after a save. */
    private static Collection<Object> events(Object aggregate) {
        return ReflectionTestUtils.invokeMethod(aggregate, "domainEvents");
    }

    @Test
    void namedConstructorsSetIdentity() {
        MerchantStore byName = new MerchantStore(STORE, NAME);
        MerchantStore byEmail = new MerchantStore(STORE, NAME, EMAIL);

        assertThat(byName.getId()).isEqualTo(STORE);
        assertThat(byName.getStorename()).isEqualTo(NAME);
        assertThat(byName.getStoreEmailAddress()).isNull();
        assertThat(byEmail.getStoreEmailAddress()).isEqualTo(EMAIL);
    }

    @Test
    void freshRowCarriesUsableDefaults() {
        MerchantStore store = new MerchantStore();

        assertThat(store.getWeightunitcode()).isEqualTo(MeasureUnit.LB.name());
        assertThat(store.getSeizeunitcode()).isEqualTo(MeasureUnit.IN.name());
        assertThat(store.getInBusinessSince()).isEqualTo(LocalDate.now());
        assertThat(store.getAuditSection()).isNotNull();
        assertThat(store.getLanguages()).isEmpty();
        assertThat(store.getStoreDomains()).isEmpty();
        assertThat(store.isUseCache()).isFalse();
        assertThat(store.isRequireLoginForOrderPlacement()).isFalse();
    }


    @Test
    void aSaveRaisesStoreChangedForEveryServiceThatHoldsACopy() {
        MerchantStore store = new MerchantStore(STORE, NAME);

        assertThat(store.changed()).isSameAs(store);

        assertThat(events(store)).containsExactly(new StoreChanged(STORE));
    }
}
