package com.asrevo.cvhome.uaa.domain.user;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * An invitation to create an account someone else will finish.
 *
 * <p>
 * No password: the account exists in uaa as PENDING until the person follows the link and sets one. The roles are
 * granted at that moment, not before, so an invitation that is never accepted grants nothing.
 * </p>
 */
public record InviteUserRequest(String email, String username, String firstName, String lastName, List<String> roles,
                                Map<String, Object> metadata) implements Serializable {
}
