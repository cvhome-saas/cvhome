package com.asrevo.cvhome.catalog.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.asrevo.cvhome.catalog.model.product.event.BrandRenamedEvent;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A brand's name is part of the search document of every product carrying it, so a rename invalidates all of
 * them. {@code names()} is what tells an actual rename from a save that only moved the code or the sort order
 * — and the difference matters, because the wrong answer either rebuilds a whole catalogue for nothing or
 * leaves every one of its products findable only under the old name.
 */
class BrandRenameEventsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode AR = new LanguageCode("ar");

    private static final String NIKE = "Nike";

    private static final String NIKE_AR = "نايكي";

    private static Manufacturer brand(Object... languagesAndNames) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(7L);
        manufacturer.setCode("NIKE");
        manufacturer.setStoreMerchantId(STORE);
        Set<ManufacturerDescription> descriptions = new HashSet<>();
        for (int i = 0; i < languagesAndNames.length; i += 2) {
            ManufacturerDescription description = new ManufacturerDescription();
            description.setLanguageCode((LanguageCode) languagesAndNames[i]);
            description.setName((String) languagesAndNames[i + 1]);
            description.setManufacturer(manufacturer);
            descriptions.add(description);
        }
        manufacturer.setDescriptions(descriptions);
        return manufacturer;
    }

    private static Collection<?> events(Manufacturer manufacturer) {
        return ReflectionTestUtils.invokeMethod(manufacturer, "domainEvents");
    }

    @Test
    void namesAreReadPerLanguage() {
        assertThat(brand(EN, NIKE, AR, NIKE_AR).names())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(EN, NIKE, AR, NIKE_AR));
    }

    /**
     * The comparison is what decides whether to rebuild, so an unchanged brand has to compare equal — not
     * merely look similar.
     */
    @Test
    void anUnchangedBrandComparesEqual() {
        assertThat(brand(EN, NIKE, AR, NIKE_AR).names()).isEqualTo(brand(AR, NIKE_AR, EN, NIKE).names());
    }

    @Test
    void aRenameInAnySingleLanguageIsDetected() {
        assertThat(brand(EN, NIKE, AR, NIKE_AR).names()).isNotEqualTo(brand(EN, NIKE, AR, "نايك").names());
    }

    @Test
    void addingATranslationCountsAsARename() {
        assertThat(brand(EN, NIKE).names()).isNotEqualTo(brand(EN, NIKE, AR, NIKE_AR).names());
    }

    /**
     * A description row with no language or no name yet cannot be compared, and must not blow up the save
     * that is creating it.
     */
    @Test
    void halfWrittenDescriptionsAreIgnoredRatherThanThrowing() {
        Manufacturer manufacturer = brand(EN, NIKE);
        ManufacturerDescription blank = new ManufacturerDescription();
        blank.setManufacturer(manufacturer);
        manufacturer.getDescriptions().add(blank);

        assertThat(manufacturer.names()).containsExactlyEntriesOf(java.util.Map.of(EN, NIKE));
    }

    @Test
    void aBrandWithNoCopyHasNoNames() {
        assertThat(brand().names()).isEmpty();
    }

    @Test
    void renamingAnnouncesItToTheOutbox() {
        Manufacturer manufacturer = brand(EN, NIKE).renamed();

        assertThat(events(manufacturer)).singleElement()
                .isInstanceOfSatisfying(BrandRenamedEvent.class, event -> {
                    assertThat(event.manufacturerId()).isEqualTo(7L);
                    assertThat(event.storeId()).isEqualTo(STORE.getId());
                    // The outbox partitions on a string; a raw Long would be rejected at publish time.
                    assertThat(event.partitionKey()).isEqualTo("7");
                });
    }

    @Test
    void aBrandNobodyRenamedAnnouncesNothing() {
        assertThat(events(brand(EN, NIKE))).isEmpty();
    }
}
