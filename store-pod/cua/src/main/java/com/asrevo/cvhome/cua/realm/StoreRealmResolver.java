package com.asrevo.cvhome.cua.realm;

import java.util.Objects;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmResolver;

/**
 * Which store's shopper this request belongs to.
 *
 * <p>
 * <strong>The host decides.</strong> The pod edge resolves the storefront host to a store and injects
 * {@code Store-Id}; that is the one source that a caller cannot choose. Everything else a request carries — the
 * {@code client_id} on a login form, the {@code ?store=} on a JSON call — is supplied by whoever made the request,
 * and cua's old behaviour of reading the tenant straight out of the form field is what this replaces.
 * </p>
 *
 * <p>
 * Those values are still read, because not every caller arrives through the edge and the JSON APIs are addressed
 * by {@code ?store=} across the whole repo. But when the edge has spoken and the request disagrees with it, the
 * request is refused rather than resolved to either one: a form on store A's page naming store B is either a
 * misconfiguration or an attempt to act in another merchant's realm, and quietly picking a winner would make the
 * second one work.
 * </p>
 *
 * <p>
 * Some endpoints belong to no realm — the JWKS document and the discovery metadata are one per pod, the same for
 * every store — and those resolve to {@code null} so that nothing realm-scoped can run under a guessed realm.
 * </p>
 */
public class StoreRealmResolver implements RealmResolver {

    /** Injected by the pod edge after it resolves the storefront host to a store. */
    public static final String STORE_HEADER = "Store-Id";

    static final String STORE_PARAM = "store";

    static final String CLIENT_ID_PARAM = "client_id";

    private static final Set<String> REALM_INDEPENDENT =
            Set.of("/oauth2/jwks", "/.well-known", "/actuator", "/error");

    @Override
    public RealmId resolve(HttpServletRequest request) {
        if (realmIndependent(request)) {
            return null;
        }
        String fromEdge = trimmed(request.getHeader(STORE_HEADER));
        String claimed = firstOf(trimmed(request.getParameter(STORE_PARAM)),
                trimmed(request.getParameter(CLIENT_ID_PARAM)));

        if (fromEdge == null) {
            return claimed == null ? null : RealmId.of(claimed);
        }
        if (claimed != null && !claimed.equals(fromEdge)) {
            throw new CrossStoreRequestException(fromEdge, claimed);
        }
        return RealmId.of(fromEdge);
    }

    private boolean realmIndependent(HttpServletRequest request) {
        String path = request.getRequestURI();
        // The context path is /cua behind the edge, so match on a suffix rather than an exact path.
        return REALM_INDEPENDENT.stream().anyMatch(path::contains);
    }

    private static String firstOf(String preferred, String fallback) {
        return Objects.nonNull(preferred) ? preferred : fallback;
    }

    private static String trimmed(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
