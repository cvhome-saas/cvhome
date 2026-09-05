package com.asrevo.cvhome.s2s.config.internal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.MalformedStoreIdException;
import com.asrevo.cvhome.errors.MissingStoreParameterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Where the {@code store} query parameter becomes a {@link StoreMerchantId}.
 *
 * <p>
 * {@code StoreMerchantId} is deliberately unvalidated as a type — the security layer uses {@code "*"} as a
 * wildcard and cua derives ids from client ids — so this resolver is the HTTP edge that does validate. A malformed
 * id has to be a typed 400 here rather than reaching a repository and matching nothing, which is a 404 that tells
 * the caller their id was right and the store was missing.
 * </p>
 */
class StoreArgumentResolverTest {

    private static final String HEX = "65f023632bc46470c104b76f";
    private static final String STORE_PARAM = "store";

    private final ServletStoreMerchantIdArgumentResolver resolver = new ServletStoreMerchantIdArgumentResolver();
    private final NativeWebRequest request = Mockito.mock(NativeWebRequest.class);

    private static MethodParameter parameterOfType(Class<?> type) {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Mockito.<Class<?>>when(parameter.getParameterType()).thenReturn(type);
        return parameter;
    }

    @Test
    void itAnswersOnlyForAStoreMerchantIdParameter() {
        assertThat(resolver.supportsParameter(parameterOfType(StoreMerchantId.class))).isTrue();
        assertThat(resolver.supportsParameter(parameterOfType(LanguageCode.class))).isFalse();
        assertThat(resolver.supportsParameter(parameterOfType(String.class))).isFalse();
    }

    @Test
    void avalidHexIdBecomesAStoreMerchantId() throws Exception {
        when(request.getParameter(STORE_PARAM)).thenReturn(HEX);

        assertThat(resolver.resolveArgument(null, null, request, null)).isEqualTo(new StoreMerchantId(HEX));
    }

    @Test
    void anAbsentOrEmptyStoreParameterIsATypedMissingParameter() {
        when(request.getParameter(STORE_PARAM)).thenReturn(null);
        assertThatThrownBy(() -> resolver.resolveArgument(null, null, request, null))
                .isInstanceOf(MissingStoreParameterException.class);

        when(request.getParameter(STORE_PARAM)).thenReturn("");
        assertThatThrownBy(() -> resolver.resolveArgument(null, null, request, null))
                .isInstanceOf(MissingStoreParameterException.class);
    }

    @Test
    void amalformedIdIsAFourHundredRatherThanAFourOhFourFromTheRepository() {
        when(request.getParameter(STORE_PARAM)).thenReturn("not-an-object-id");

        // A 404 here would tell the caller their id was right and the store was missing.
        assertThatThrownBy(() -> resolver.resolveArgument(null, null, request, null))
                .isInstanceOf(MalformedStoreIdException.class);
    }

    @Test
    void theWildcardSentinelIsNotAValidStoreParameterEither() {
        // "*" is meaningful inside a token, never on the wire.
        when(request.getParameter(STORE_PARAM)).thenReturn("*");

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, request, null))
                .isInstanceOf(MalformedStoreIdException.class);
    }
}
