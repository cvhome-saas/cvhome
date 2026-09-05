package com.asrevo.cvhome.checkout.services.reference;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableCountry;

import static org.assertj.core.api.Assertions.assertThat;

class CountryServiceImplTest {

    private static final String DE_2 = "DE";

    private final CountryServiceImpl service = new CountryServiceImpl();

    @Test
    void listsEveryIsoCountryNamedInTheLanguageSortedByName() {
        List<ReadableCountry> en = service.all(LanguageCode.defaultLanguage());
        List<ReadableCountry> fr = service.all(new LanguageCode("fr"));

        assertThat(en).hasSizeGreaterThan(200);
        assertThat(en).extracting(c -> c.getCode().isoCode()).contains(DE_2, "EG", "US");
        assertThat(en.stream().filter(c -> c.getCode().isoCode().equals(DE_2)).findFirst()).get()
                .satisfies(c -> {
                    assertThat(c.getName()).isEqualTo("Germany");
                    assertThat(c.isSupported()).isTrue();
                    assertThat(c.getZones()).isEmpty();
                    assertThat(c.getId()).isPositive();
                });
        assertThat(fr.stream().filter(c -> c.getCode().isoCode().equals(DE_2)).findFirst()).get()
                .satisfies(c -> assertThat(c.getName()).isEqualTo("Allemagne"));
        assertThat(en).extracting(ReadableCountry::getName)
                .isSortedAccordingTo((a, b) -> java.text.Collator.getInstance(java.util.Locale.ENGLISH).compare(a, b));
        assertThat(service.all(LanguageCode.defaultLanguage())).as("memoised").isSameAs(en);
    }

    @Test
    void anUnknownOrWildcardLanguageFallsBackToTheDefault() {
        assertThat(service.all(null)).isSameAs(service.all(LanguageCode.defaultLanguage()));
        assertThat(service.all(LanguageCode.allLanguage())).isSameAs(service.all(LanguageCode.defaultLanguage()));
    }

    @Test
    void knowsIsoCodesCaseInsensitively() {
        assertThat(service.isKnown("gb")).isTrue();
        assertThat(service.isKnown("GB")).isTrue();
        assertThat(service.isKnown("XX")).isFalse();
        assertThat(service.isKnown(null)).isFalse();
    }
}
