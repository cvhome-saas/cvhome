package com.asrevo.cvhome.uaa.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The signed-in principal, as {@code /api/v1/auth/me} describes it.
 *
 * <p>
 * A DTO rather than the {@code Authentication} itself: serialising the principal handed a bearer caller the whole
 * decoded JWT, headers and raw token value included, and a form-login caller a {@code UserDetails}. {@code authorities}
 * keeps the {@code {authority}} shape the consoles' {@code AuthService} already reads.
 * </p>
 *
 * @param authenticatedVia {@code SESSION} for a form login, {@code JWT} for a bearer token
 */
public record MeResponse(UUID uid, String username, String email, String firstName, String lastName,
                         Set<String> roles, Set<String> permissions, List<AuthorityDto> authorities,
                         String authenticatedVia) {

    public record AuthorityDto(String authority) {
    }

}
