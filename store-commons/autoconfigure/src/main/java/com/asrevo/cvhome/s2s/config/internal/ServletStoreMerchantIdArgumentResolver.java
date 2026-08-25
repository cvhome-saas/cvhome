package com.asrevo.cvhome.s2s.config.internal;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.MalformedStoreIdException;
import com.asrevo.cvhome.errors.MissingStoreParameterException;

import lombok.RequiredArgsConstructor;

/**
 * Supplies the {@code StoreMerchantId} every store-scoped endpoint takes, from the {@code store} query parameter.
 *
 * <p>
 * The only place a store id enters from outside, and therefore the only sensible place to check that it is one.
 * {@link StoreMerchantId} itself stays unvalidated — the security layer uses a non-hex sentinel for "every store" —
 * so a bad value that gets past here is not refused until something far inside tries to make sense of it.
 * </p>
 */
@RequiredArgsConstructor
public class ServletStoreMerchantIdArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String REQUEST_PARAMETER_STORE = "store";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(StoreMerchantId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
            throws MissingStoreParameterException, MalformedStoreIdException {
        String storeCode = Optional.ofNullable(webRequest.getParameter(REQUEST_PARAMETER_STORE))
                .filter(it -> !it.isEmpty())
                .orElseThrow(() -> MissingStoreParameterException.of(REQUEST_PARAMETER_STORE));

        if (!ObjectId.isValid(storeCode)) {
            throw MalformedStoreIdException.of(storeCode);
        }

        return new StoreMerchantId(storeCode);
    }

}
