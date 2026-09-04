package com.asrevo.cvhome.sso.realm;

import java.util.List;

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

    /**
     * Roles every principal of this deployment carries, on top of any granted to them individually.
     *
     * <p>
     * cua sets {@code CUSTOMER}: every account it holds is a shopper, by construction, and the old cua wrote that
     * role into each token as a literal. uaa leaves it empty — its principals differ from one another, so their
     * roles are rows, not a property.
     * </p>
     */
    private List<String> defaultRoles = List.of();

    public RealmId fixedRealm() {
        return RealmId.of(fixed);
    }

    public boolean single() {
        return RealmMode.SINGLE.equals(mode);
    }

}
