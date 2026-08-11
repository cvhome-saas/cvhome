package com.asrevo.cvhome.catalog.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Refuses writes to a store whose subscription has lapsed.
 *
 * <p>
 * The layer that actually holds. The gateway's equivalent is a convenience — it works from a list refreshed on a
 * timer, and a gateway that started during a billing outage has no list at all — whereas this sits in the service
 * that owns the data and asks about the specific store being written to.
 * </p>
 *
 * <p>
 * Reads are deliberately untouched. A seller whose payment has lapsed must still be able to see their catalog, and
 * their storefront must still be able to serve it; taking the shop offline is what makes a recoverable billing
 * problem into a lost customer. Only changing things is refused.
 * </p>
 *
 * <p>
 * Registered by this pod's own {@code WebMvcConfigurer}, not by the shared one in {@code store-commons:autoconfigure}
 * — that belongs to the platform and must not learn what billing is.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreBillingWriteGate implements WebMvcConfigurer {

    /**
     * Only the seller-facing paths. The storefront's own reads live elsewhere and are none of this gate's business.
     */
    private static final List<String> GUARDED = List.of("/api/*/private/**");

    private static final Set<String> READ_METHODS = Set.of(HttpMethod.GET.name(), HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name());

    private static final String STORE_PARAM = "store";

    private static final String BODY = """
            {"code":"BILLING.STORE.SUSPENDED","category":"PAYMENT_REQUIRED",\
            "title":"BILLING.STORE.SUSPENDED",\
            "detail":"This store's subscription is not active, so it cannot be changed."}""";

    private final StoreEntitlements storeEntitlements;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new Interceptor(storeEntitlements)).addPathPatterns(GUARDED);
    }

    @RequiredArgsConstructor
    static class Interceptor implements HandlerInterceptor {

        private final StoreEntitlements storeEntitlements;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws IOException {
            if (READ_METHODS.contains(request.getMethod())) {
                return true;
            }
            String store = request.getParameter(STORE_PARAM);
            if (store == null || storeEntitlements.operable(new StoreMerchantId(store))) {
                return true;
            }
            log.info("Refusing {} {} — store {} has no active subscription", request.getMethod(),
                    request.getRequestURI(), store);
            response.setStatus(HttpStatus.PAYMENT_REQUIRED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(BODY);
            return false;
        }

    }

}
