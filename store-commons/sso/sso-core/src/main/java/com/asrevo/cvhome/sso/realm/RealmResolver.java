package com.asrevo.cvhome.sso.realm;

import jakarta.servlet.http.HttpServletRequest;

import com.asrevo.cvhome.commons.domain.RealmId;

/**
 * Decides which realm a request belongs to. One of the two seams between the shared server and its deployments.
 *
 * <p>
 * uaa answers with a constant. cua answers from the store the request arrived for, which is the whole reason the
 * seam exists: a shopper reaches cua same-origin on their store's own host, so the host is what identifies the
 * user pool.
 * </p>
 */
public interface RealmResolver {

    /**
     * The realm this request belongs to, or {@code null} when the request is not realm-scoped at all — the JWKS
     * document and the discovery endpoint are the same for every realm the deployment serves.
     */
    RealmId resolve(HttpServletRequest request);

}
