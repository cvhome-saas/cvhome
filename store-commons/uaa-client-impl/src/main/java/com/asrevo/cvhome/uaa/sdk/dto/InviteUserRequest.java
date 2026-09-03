package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.List;
import java.util.Map;

/**
 * An invitation: the account is created without a password and the person sets one by following a one-time link.
 *
 * @param roles    granted when the invitation is accepted, not before
 * @param metadata the open bag — tenancy stamps {@code org} and {@code store} here
 */
public record InviteUserRequest(String email, String username, String firstName, String lastName, List<String> roles,
                                Map<String, Object> metadata) {
}
