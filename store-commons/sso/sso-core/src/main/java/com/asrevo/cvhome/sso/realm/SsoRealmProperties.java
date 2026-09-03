package com.asrevo.cvhome.sso.realm;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.asrevo.cvhome.commons.domain.RealmId;

import lombok.Getter;
import lombok.Setter;

/**
 * Which shape of SSO server this deployment is.
 *
 * <p>
 * uaa sets {@code mode: SINGLE} and never changes it; cua sets {@code mode: MULTI}. There is no default on
 * purpose — a deployment that forgot to say would otherwise silently pick one, and the two behave very
 * differently about where a user is looked up.
 * </p>
 */
@ConfigurationProperties("com.asrevo.cvhome.sso.realm")
@Getter
@Setter
public class SsoRealmProperties {

    /** {@code SINGLE} for one fixed realm, {@code MULTI} for one realm per store. Required. */
    private RealmMode mode;

    /** The realm id in {@code SINGLE} mode. Ignored in {@code MULTI}. */
    private String fixed = RealmId.PLATFORM.getId();

    public RealmId fixedRealm() {
        return RealmId.of(fixed);
    }

    public boolean single() {
        return RealmMode.SINGLE.equals(mode);
    }

}
