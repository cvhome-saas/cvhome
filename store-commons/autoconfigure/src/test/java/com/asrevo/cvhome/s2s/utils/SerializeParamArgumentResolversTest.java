package com.asrevo.cvhome.s2s.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.service.invoker.HttpRequestValues;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * How a value object becomes a query parameter on an outgoing {@code @HttpExchange} call.
 *
 * <p>
 * These are the client-side mirror of the argument resolvers on the server. The parameter names are the contract:
 * every endpoint reads {@code store} and {@code lang}, so a resolver that spelled either differently would send a
 * request the server answers with "missing store parameter" — from a caller that plainly passed one.
 * </p>
 *
 * <p>
 * Each resolver answers {@code false} for anything that is not its own type, because they are consulted in turn
 * and the first {@code true} wins. One that claimed an argument it could not serialise would silently drop it.
 * </p>
 */
class SerializeParamArgumentResolversTest {

    private static final String HEX = "65f023632bc46470c104b76f";
    private static final String OTHER = "not mine";
    private static final String ARABIC = "ar";
    private static final String HOST = "shop.example.com";

    private final HttpRequestValues.Builder values = Mockito.mock(HttpRequestValues.Builder.class);

    @Test
    void aStoreIsSentAsTheStoreParameterEveryEndpointReads() {
        assertThat(new StoreMerchantIdSerializeParamArgumentResolver()
                .resolve(new StoreMerchantId(HEX), null, values)).isTrue();

        verify(values).addRequestParameter("store", HEX);
    }

    @Test
    void aLanguageIsSentAsLangNotAsLanguage() {
        assertThat(new LanguageCodeSerializeParamArgumentResolver()
                .resolve(new LanguageCode(ARABIC), null, values)).isTrue();

        verify(values).addRequestParameter("lang", ARABIC);
    }

    @Test
    void anOrgIsSentAsItsHexUnderOrgId() {
        ManagerOrgId org = new ManagerOrgId("21f023932bc66470c104b76f");

        assertThat(new OrgSerializeParamArgumentResolver().resolve(org, null, values)).isTrue();

        verify(values).addRequestParameter("org-id", org.getId().toString());
    }

    @Test
    void adomainIsSentUnderItsOwnName() {
        assertThat(new DomainSerializeParamArgumentResolver()
                .resolve(new Domain(HOST), null, values)).isTrue();

        verify(values).addRequestParameter("domain", HOST);
    }

    @Test
    void aPageableTravelsAsTwoSeparateParameters() {
        assertThat(new PageableSerializeParamArgumentResolver()
                .resolve(PageRequest.of(2, 50), null, values)).isTrue();

        verify(values).addRequestParameter("page", "2");
        verify(values).addRequestParameter("size", "50");
    }

    @Test
    void eachResolverDeclinesAnArgumentThatIsNotItsOwnType() {
        // They are consulted in turn and the first true wins; claiming one it cannot serialise drops it silently.
        assertThat(new StoreMerchantIdSerializeParamArgumentResolver().resolve(OTHER, null, values)).isFalse();
        assertThat(new LanguageCodeSerializeParamArgumentResolver().resolve(OTHER, null, values)).isFalse();
        assertThat(new OrgSerializeParamArgumentResolver().resolve(OTHER, null, values)).isFalse();
        assertThat(new DomainSerializeParamArgumentResolver().resolve(OTHER, null, values)).isFalse();
        assertThat(new PageableSerializeParamArgumentResolver().resolve(OTHER, null, values)).isFalse();
        Mockito.verifyNoInteractions(values);
    }

    @Test
    void anOrgWithNoIdIsDeclinedRatherThanThrowingIntoTheCallersStack() {
        // getId() is null there, and the resolver's catch turns the NPE into "not mine".
        assertThat(new OrgSerializeParamArgumentResolver()
                .resolve(new ManagerOrgId((String) null), null, values)).isFalse();
    }
}
