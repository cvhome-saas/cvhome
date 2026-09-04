package com.asrevo.cvhome.cua.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.cua.web.dto.ReadableSocialLogin;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;

import lombok.RequiredArgsConstructor;

/**
 * The providers a storefront's login page offers, in the shape landing-ui already reads.
 *
 * <p>
 * <strong>What is left of it.</strong> cua used to keep its own {@code social_login_configs} table with three
 * providers and two fields each, and this class was the translation that let the old console screen go on
 * working. The console addresses the identity-provider API directly now, so the writing half is gone; what
 * remains is the storefront's own list, whose shape landing-ui reads and which is kept exactly as it was.
 * </p>
 *
 * <p>
 * Nothing here names a store. The realm is resolved from the request, and every query and write below is scoped to
 * it by Hibernate's tenant filter — which is what makes one merchant's Google application invisible to another.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SocialLoginConfigService {

    private final IdentityProviderService providers;

    /**
     * The buttons a storefront's login page renders, for the store the request arrived for.
     *
     * <p>
     * The registration id is the bare alias now, not {@code {store}.{provider}}. It no longer has to carry the
     * store because the alias is unique within its realm and the realm comes from the host — and the callback URL
     * a merchant registers with Google is still per-store, because it is built on their own domain.
     * </p>
     */
    public List<ReadableSocialLogin> enabledLogins() {
        return providers.visibleForLogin().stream()
                .map(p -> new ReadableSocialLogin(p.alias(), p.displayName(), p.alias()))
                .toList();
    }

}
