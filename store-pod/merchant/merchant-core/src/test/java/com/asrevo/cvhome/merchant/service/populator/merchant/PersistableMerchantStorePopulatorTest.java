package com.asrevo.cvhome.merchant.service.populator.merchant;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ColorTheme;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.Theme;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.asrevo.cvhome.store.model.references.PersistableBaseAddress;
import com.asrevo.cvhome.store.model.references.WeightUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Request-to-entity mapping. Every branch here is a null guard over already-validated input, so the cases pin which
 * fields a partial request leaves at the entity's defaults rather than nulling them out.
 */
class PersistableMerchantStorePopulatorTest {

    private static final String STORE_ID = "65f023632bc46470c104b76f";

    private static final String NAME = "Riyadh Fashion Hub";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String EMAIL = "hello@example.com";

    private static final String PHONE = "+966 11 000 0000";

    private static final String LOGO = "logo.jpeg";

    private static final String BANNER = "banner.jpeg";

    private static final String TEMPLATE = "modern";

    private static final String CITY = "Riyadh";

    private static final String POSTAL_CODE = "12345";

    private static final String STREET = "1 King Fahd Road";

    private static final String SUB_DOMAIN = "riyadh";

    private static final String X = "X";

    private static final String BLANK = " ";

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode AR = new LanguageCode("ar");

    private static final CurrencyCode SAR = new CurrencyCode("SAR");

    private static final CountryIsoCode SA = new CountryIsoCode("SA");

    private static final ZoneCode RIYADH = new ZoneCode("RUH");

    private static final SliderImage SLIDE = new SliderImage(0, "slide-1.jpeg");

    private final PersistableMerchantStorePopulator populator =
            new PersistableMerchantStorePopulator(mock(MerchantStoreService.class));

    private static PersistableMerchantStore fullRequest() {
        PersistableMerchantStore source = new PersistableMerchantStore();
        source.setId(STORE_ID);
        source.setName(NAME);
        source.setOrg(ORG);
        source.setEmail(EMAIL);
        source.setPhone(PHONE);
        source.setTheme(Theme.MODERN);
        source.setColorTheme(ColorTheme.OCEAN);
        source.setTemplate(TEMPLATE);
        source.setInBusinessSince(LocalDate.of(2024, 3, 31));
        source.setDimension(MeasureUnit.CM);
        source.setWeight(WeightUnit.KG);
        source.setCurrency(SAR);
        source.setCurrencyFormatNational(true);
        source.setUseCache(true);
        source.setRequireLoginForOrderPlacement(true);
        source.setDefaultLanguage(AR);
        source.setSupportedLanguages(List.of(AR.code(), EN.code()));
        source.setStoreDomains(Set.of(new ManagerStoreDomain(SUB_DOMAIN, DomainType.SUB_DOMAIN)));
        PersistableBaseAddress address = new PersistableBaseAddress();
        address.setAddress(STREET);
        address.setCity(CITY);
        address.setCountry(SA);
        address.setPostalCode(POSTAL_CODE);
        address.setStateProvince(RIYADH);
        source.setAddress(address);
        return source;
    }

    private static PersistableMerchantStore minimalRequest() {
        PersistableMerchantStore source = new PersistableMerchantStore();
        source.setName(NAME);
        source.setEmail(EMAIL);
        source.setPhone(PHONE);
        source.setDefaultLanguage(new LanguageCode(BLANK));
        source.setSupportedLanguages(List.of());
        source.setTemplate(BLANK);
        return source;
    }

    @Test
    void fullRequestMapsEveryField() {
        MerchantStore target = populator.populate(fullRequest(), new MerchantStore(), new MerchantStore(), EN);

        assertThat(target.getId()).isEqualTo(new StoreMerchantId(STORE_ID));
        assertThat(target.getStorename()).isEqualTo(NAME);
        assertThat(target.getOrg()).isEqualTo(ORG);
        assertThat(target.getStoreEmailAddress()).isEqualTo(EMAIL);
        assertThat(target.getStorephone()).isEqualTo(PHONE);
        assertThat(target.getTheme()).isEqualTo(Theme.MODERN);
        assertThat(target.getColorTheme()).isEqualTo(ColorTheme.OCEAN);
        assertThat(target.getStoreTemplate()).isEqualTo(TEMPLATE);
        assertThat(target.getInBusinessSince()).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(target.getSeizeunitcode()).isEqualTo(MeasureUnit.CM.name());
        assertThat(target.getWeightunitcode()).isEqualTo(WeightUnit.KG.name());
        assertThat(target.getCurrency()).isEqualTo(SAR);
        assertThat(target.isCurrencyFormatNational()).isTrue();
        assertThat(target.isUseCache()).isTrue();
        assertThat(target.isRequireLoginForOrderPlacement()).isTrue();
        assertThat(target.getDefaultLanguageCode()).isEqualTo(AR);
        assertThat(target.getLanguages()).containsExactly(AR, EN);
        assertThat(target.getStoreDomains()).extracting(ManagerStoreDomain::domain).containsExactly(SUB_DOMAIN);
        assertThat(target.getStoreaddress()).isEqualTo(STREET);
        assertThat(target.getStorecity()).isEqualTo(CITY);
        assertThat(target.getCountry()).isEqualTo(SA);
        assertThat(target.getStorepostalcode()).isEqualTo(POSTAL_CODE);
        assertThat(target.getZone()).isEqualTo(RIYADH);
        assertThat(target.getStorestateprovince()).isEqualTo(RIYADH);
    }

    @Test
    void partialRequestKeepsEntityDefaults() {
        MerchantStore target = populator.populate(minimalRequest(), new MerchantStore(), new MerchantStore(), EN);

        assertThat(target.getId()).isNull();
        assertThat(target.getOrg()).isNull();
        assertThat(target.getStoreTemplate()).isNull();
        assertThat(target.getDefaultLanguageCode()).isNull();
        assertThat(target.getSeizeunitcode()).isEqualTo(MeasureUnit.IN.name());
        assertThat(target.getWeightunitcode()).isEqualTo(MeasureUnit.LB.name());
        assertThat(target.getLanguages()).isEmpty();
        assertThat(target.getStoreDomains()).isEmpty();
        assertThat(target.getStorecity()).isNull();
        assertThat(target.getCountry()).isNull();
    }

    @Test
    void missingTargetIsCreated() {
        MerchantStore target = populator.populate(fullRequest(), null, new MerchantStore(), EN);

        assertThat(target).isNotNull();
        assertThat(target.getStorename()).isEqualTo(NAME);
    }

    @Test
    void twoArgumentFormBuildsAFreshEntity() {
        MerchantStore stored = new MerchantStore();
        stored.setStorename("Old name");

        MerchantStore target = populator.populate(fullRequest(), stored, EN);

        assertThat(target).isNotSameAs(stored);
        assertThat(target.getStorename()).isEqualTo(NAME);
    }

}
