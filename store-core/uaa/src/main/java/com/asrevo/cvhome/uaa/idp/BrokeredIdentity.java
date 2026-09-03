package com.asrevo.cvhome.uaa.idp;

import java.util.Map;

/**
 * What a provider said about the person, after the alias's attribute mapping: the stable subject, the email and
 * whether the provider vouches for it, and the names.
 */
public record BrokeredIdentity(String subject, String email, boolean emailVerified, String firstName, String lastName,
                               Map<String, Object> attributes) {
}
