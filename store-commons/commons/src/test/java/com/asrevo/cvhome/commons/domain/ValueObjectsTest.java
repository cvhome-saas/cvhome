package com.asrevo.cvhome.commons.domain;

import java.util.Currency;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.commons.utils.DefaultStoresConstants;
import com.asrevo.cvhome.commons.utils.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The small value objects, and the handful of them that are not as inert as they look.
 *
 * <p>
 * Three carry behaviour worth pinning: {@link SocialLink} validates its provider in a compact constructor,
 * {@link SliderImage} and {@link SocialLink} both sort <em>descending</em> (the comparison is inverted on purpose, so
 * the highest-priority slide renders first), and {@link ServiceDomain} builds the {@code lb://} host strings that
 * service-to-service calls are addressed with.
 * </p>
 */
class ValueObjectsTest {

    private static final String HEX = "65f023632bc46470c104b76f";
    private static final String URL = "https://example.com";
    private static final String LOW = "aaa";
    private static final String HIGH = "bbb";
    private static final String EGYPT = "EG";
    private static final String USD = "USD";
    private static final String IDENTITY = "auth0|123";
    private static final String POD_NAME = "pod-1";
    private static final String ORDERS = "orders";
    private static final String A_DATE = "2026-01-01";
    private static final String CODE = "STORE.NOT_FOUND";
    private static final String MESSAGE = "no such store";
    private static final String ADDRESS = "someone@example.com";

    @Test
    void aSocialLinkRefusesAProviderThatIsNotOne() {
        assertThatThrownBy(() -> new SocialLink("MYSPACE", URL))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(SocialProvider.class)
    void everySocialProviderIsAcceptedAsALink(SocialProvider provider) {
        assertThat(new SocialLink(provider.name(), URL).provider()).isEqualTo(provider.name());
    }

    @Test
    void socialLinksAndSliderImagesSortDescendingSoTheTopOneRendersFirst() {
        assertThat(new SocialLink("FACEBOOK", LOW).compareTo(new SocialLink("X", HIGH))).isPositive();
        assertThat(new SliderImage(1, LOW).compareTo(new SliderImage(2, HIGH))).isPositive();
    }

    @Test
    void aServiceDomainBuildsBothHostShapes() {
        ServiceDomain domain = new ServiceDomain("catalog", "gateway.com", "8080", "http", "ns", "gw");
        assertThat(domain.getServiceHost()).isEqualTo("http://gateway.com:8080");
        assertThat(domain.getServiceHost("spg")).isEqualTo("http://gateway.com:8080/spg/");
    }

    @Test
    void aCountryIsoCodeIsValidOnlyWhenItCarriesAValue() {
        assertThat(new CountryIsoCode(EGYPT).isValid()).isTrue();
        assertThat(new CountryIsoCode("").isValid()).isFalse();
        assertThat(new CountryIsoCode(null).isValid()).isFalse();
    }

    @Test
    void aCurrencyCodeResolvesToAJavaCurrency() {
        assertThat(new CurrencyCode(USD).getCurrencyInstance()).isEqualTo(Currency.getInstance(USD));
        assertThatThrownBy(() -> new CurrencyCode("NOPE").getCurrencyInstance())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theCodeValueObjectsSortByTheirValue() {
        assertThat(new CurrencyCode("EUR").compareTo(new CurrencyCode(USD))).isNegative();
        assertThat(new ZoneCode(LOW).compareTo(new ZoneCode(HIGH))).isNegative();
        assertThat(new CountryIsoCode(EGYPT).compareTo(new CountryIsoCode("FR"))).isNegative();
    }

    @Test
    void storeDomainsSortCaseInsensitivelyBecauseHostnamesAre() {
        ManagerStoreDomain lower = new ManagerStoreDomain("shop.example.com", DomainType.SUB_DOMAIN);
        ManagerStoreDomain upper = new ManagerStoreDomain("SHOP.EXAMPLE.COM", DomainType.SUB_DOMAIN);
        assertThat(lower.compareTo(upper)).isZero();
    }

    @Test
    void anIdentityIdCarriesItsRawValue() {
        assertThat(IdentityId.of(IDENTITY).getId()).isEqualTo(IDENTITY);
        assertThat(IdentityId.of(IDENTITY)).isEqualTo(new IdentityId(IDENTITY));
    }

    @Test
    void anEventIdParsesAndMintsObjectIdHex() {
        assertThat(new EventId(HEX).getId()).isEqualTo(new ObjectId(HEX));
        assertThat(EventId.newId().getId()).isNotEqualTo(EventId.newId().getId());
    }

    @Test
    void aPodShortensItsIdForLogsAndToleratesNotHavingOne() {
        PodEndpoint endpoint = new PodEndpoint("http://pod-1:8080", EndpointType.INTERNAL);
        Pod pod = new Pod(new PodId(HEX), POD_NAME, endpoint, ManagerOrgId.newId(), "pod1.example.com");
        assertThat(pod.shortenPodId()).isEqualTo(HEX.substring(0, 8));
        assertThat(new Pod(null, POD_NAME, endpoint, null, null).shortenPodId()).isNull();
    }

    @Test
    void aStatisticEntryMayOrMayNotBeDated() {
        assertThat(StatisticEntry.of(A_DATE, ORDERS, 3).date()).isEqualTo(A_DATE);
        assertThat(StatisticEntry.of(ORDERS, 3).date()).isNull();
        assertThat(StatisticEntry.of(ORDERS, 3).value()).isEqualTo(3);
    }

    @Test
    void anErrorCodeCarriesItsCodeAndMessage() {
        assertThat(new ErrorCode(CODE, MESSAGE).code()).isEqualTo(CODE);
        assertThat(new ErrorCode(CODE, MESSAGE).message()).isEqualTo(MESSAGE);
    }

    @Test
    void theDefaultStoreConstantsAgreeWithTheirStringForm() {
        assertThat(DefaultStoresConstants.DEFAULT_ORG1_STORE1)
                .isEqualTo(new StoreMerchantId(DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR));
        assertThat(Set.of(DefaultStoresConstants.DEFAULT_ORG1_STORE1, DefaultStoresConstants.DEFAULT_ORG1_STORE2,
                DefaultStoresConstants.DEFAULT_ORG2_STORE1, DefaultStoresConstants.DEFAULT_ORG2_STORE2)).hasSize(4);
    }

    @Test
    void anEmailIsAThinWrapperAroundItsAddress() {
        assertThat(new Email(ADDRESS).email()).isEqualTo(ADDRESS);
    }
}
