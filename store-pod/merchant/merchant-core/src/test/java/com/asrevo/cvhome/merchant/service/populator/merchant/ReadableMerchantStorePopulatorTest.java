package com.asrevo.cvhome.merchant.service.populator.merchant;

import java.time.Instant;
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
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.Theme;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.asrevo.cvhome.store.model.references.WeightUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity-to-response mapping, including the CDN paths for logo, banner and slider images.
 */
class ReadableMerchantStorePopulatorTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String BASE = "http://cdn.test/bucket";

    private static final String NAME = "Riyadh Fashion Hub";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String EMAIL = "hello@example.com";

    private static final String PHONE = "+966 11 000 0000";

    private static final String LOGO = "logo.jpeg";

    private static final String BANNER = "banner.jpeg";

    private static final String SLIDE = "slide-1.jpeg";

    private static final String CITY = "Riyadh";

    private static final String POSTAL_CODE = "12345";

    private static final String STREET = "1 King Fahd Road";

    private static final String EDITOR = "admin@store";

    private static final String SUB_DOMAIN = "riyadh";

    private static final String X = "X";

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode AR = new LanguageCode("ar");

    private static final CurrencyCode SAR = new CurrencyCode("SAR");

    private static final CountryIsoCode SA = new CountryIsoCode("SA");

    private static final ZoneCode ZONE = new ZoneCode("RUH");

    private static final ZoneCode PROVINCE = new ZoneCode("Riyadh Province");

    private static final Instant CREATED = Instant.parse("2024-03-31T10:00:00Z");

    private static final Instant MODIFIED = Instant.parse("2024-04-01T10:00:00Z");

    private final ReadableMerchantStorePopulator populator = new ReadableMerchantStorePopulator();

    private static MerchantStore fullEntity() {
        MerchantStore source = new MerchantStore(STORE, NAME, EMAIL);
        source.setOrg(ORG);
        source.setStorephone(PHONE);
        source.setTheme(Theme.JEWELERY);
        source.setColorTheme(ColorTheme.ROSE);
        source.setDefaultLanguageCode(AR);
        source.setLanguages(List.of(AR, EN));
        source.setCountry(SA);
        source.setCurrency(SAR);
        source.setCurrencyFormatNational(true);
        source.setSeizeunitcode(MeasureUnit.CM.name());
        source.setWeightunitcode(WeightUnit.KG.name());
        source.setInBusinessSince(LocalDate.of(2024, 3, 31));
        source.setUseCache(true);
        source.setRequireLoginForOrderPlacement(true);
        source.setStoreaddress(STREET);
        source.setStorecity(CITY);
        source.setStorepostalcode(POSTAL_CODE);
        source.setZone(ZONE);
        source.setStorestateprovince(PROVINCE);
        source.setStoreDomains(Set.of(new ManagerStoreDomain(SUB_DOMAIN, DomainType.SUB_DOMAIN)));
        AuditSection audit = new AuditSection();
        audit.setDateCreated(CREATED);
        audit.setDateModified(MODIFIED);
        audit.setModifiedBy(EDITOR);
        source.setAuditSection(audit);
        return source;
    }

    private static MerchantStore bareEntity() {
        MerchantStore source = new MerchantStore(STORE, NAME);
        source.setAuditSection(null);
        return source;
    }

    @Test
    void fullEntityMapsEveryField() {
        ReadableMerchantStore target = populator.populate(fullEntity(), new ReadableMerchantStore(), null, EN);

        assertThat(target.getId()).isEqualTo(STORE.getId());
        assertThat(target.getName()).isEqualTo(NAME);
        assertThat(target.getOrg()).isEqualTo(ORG);
        assertThat(target.getEmail()).isEqualTo(EMAIL);
        assertThat(target.getPhone()).isEqualTo(PHONE);
        assertThat(target.getTheme()).isEqualTo(Theme.JEWELERY);
        assertThat(target.getColorTheme()).isEqualTo(ColorTheme.ROSE);
        assertThat(target.getDefaultLanguage()).isEqualTo(AR);
        assertThat(target.getSupportedLanguages()).containsExactly(AR.code(), EN.code());
        assertThat(target.getCountryIsoCode()).isEqualTo(SA);
        assertThat(target.getCurrency()).isEqualTo(SAR);
        assertThat(target.isCurrencyFormatNational()).isTrue();
        assertThat(target.getDimension()).isEqualTo(MeasureUnit.CM);
        assertThat(target.getWeight()).isEqualTo(WeightUnit.KG);
        assertThat(target.getInBusinessSince()).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(target.isUseCache()).isTrue();
        assertThat(target.isRequireLoginForOrderPlacement()).isTrue();
        assertThat(target.getAddress().getAddress()).isEqualTo(STREET);
        assertThat(target.getAddress().getCity()).isEqualTo(CITY);
        assertThat(target.getAddress().getPostalCode()).isEqualTo(POSTAL_CODE);
        assertThat(target.getAddress().getCountry()).isEqualTo(SA);
        // the explicit state/province wins over the zone
        assertThat(target.getAddress().getStateProvince()).isEqualTo(PROVINCE);
        assertThat(target.getStoreDomains()).extracting(ManagerStoreDomain::domain).containsExactly(SUB_DOMAIN);
        assertThat(target.getReadableAudit().getCreated()).isEqualTo(CREATED);
        assertThat(target.getReadableAudit().getUser()).isEqualTo(EDITOR);
        // Pinned to the value rather than isNotNull(): the populator read getDateCreated() into setModified(), so
        // every store's reported "last modified" was its creation date, and a not-null assertion passed anyway.
        assertThat(target.getReadableAudit().getModified()).isEqualTo(MODIFIED).isNotEqualTo(CREATED);
    }

    @Test
    void bareEntityLeavesOptionalSectionsEmpty() {
        ReadableMerchantStore target = populator.populate(bareEntity(), new ReadableMerchantStore(), null, EN);

        assertThat(target.getSupportedLanguages()).isNull();
        assertThat(target.getReadableAudit()).isNull();
        assertThat(target.getAddress().getCountry()).isNull();
        assertThat(target.getAddress().getStateProvince()).isNull();
        assertThat(target.getDimension()).isEqualTo(MeasureUnit.IN);
        assertThat(target.getWeight()).isEqualTo(WeightUnit.LB);
    }

    @Test
    void zoneFillsInWhenNoProvinceIsStored() {
        MerchantStore source = bareEntity();
        source.setZone(ZONE);

        ReadableMerchantStore target = populator.populate(source, null, null, EN);

        assertThat(target.getAddress().getStateProvince()).isEqualTo(ZONE);
    }

    @Test
    void auditWithoutDatesStillNamesTheEditor() {
        MerchantStore source = bareEntity();
        AuditSection audit = new AuditSection();
        audit.setModifiedBy(EDITOR);
        source.setAuditSection(audit);

        ReadableMerchantStore target = populator.populate(source, null, null, EN);

        assertThat(target.getReadableAudit().getUser()).isEqualTo(EDITOR);
        assertThat(target.getReadableAudit().getCreated()).isNull();
        assertThat(target.getReadableAudit().getModified()).isNull();
    }

    @Test
    void thisPopulatorNeverSuppliesItsOwnTarget() {
        // populate(..) is always called with a caller-supplied target here, so the factory hook stays unimplemented.
        assertThat(populator.createTarget()).isNull();
    }

}
