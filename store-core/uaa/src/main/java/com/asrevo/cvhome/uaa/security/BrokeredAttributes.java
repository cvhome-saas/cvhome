package com.asrevo.cvhome.uaa.security;

import java.util.Map;

import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.idp.BrokeredIdentity;
import com.asrevo.cvhome.uaa.idp.IdentityProviderMapper;

/** Applies a provider's attribute mapping to what it sent. */
final class BrokeredAttributes {

    static final String EMAIL = "email";

    static final String EMAIL_VERIFIED = "email_verified";

    static final String FIRST_NAME = "firstName";

    static final String LAST_NAME = "lastName";

    private static final String SPACE = " ";

    private BrokeredAttributes() {
    }

    static BrokeredIdentity extract(IdentityProvider provider, String subject, Map<String, Object> attributes) {
        Map<String, String> targets = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : IdentityProviderMapper.mapping(provider).entrySet()) {
            Object value = attributes.get(entry.getKey());
            if (value != null) {
                targets.put(entry.getValue(), String.valueOf(value));
            }
        }
        String email = targets.get(EMAIL);
        if (email == null && attributes.get(EMAIL) != null) {
            email = String.valueOf(attributes.get(EMAIL));
        }
        String[] names = splitName(targets.get(FIRST_NAME), targets.get(LAST_NAME));
        Object verifiedClaim = attributes.get(EMAIL_VERIFIED);
        boolean verified = Boolean.TRUE.equals(verifiedClaim) || "true".equalsIgnoreCase(String.valueOf(verifiedClaim));
        return new BrokeredIdentity(subject, email, verified, names[0], names[1], attributes);
    }

    /** A GitHub-style single "name" mapped to firstName: split it once so the last name is not lost. */
    private static String[] splitName(String first, String last) {
        if (first != null && last == null && first.contains(SPACE)) {
            String[] parts = first.split(SPACE, 2);
            return new String[] {parts[0], parts[1]};
        }
        return new String[] {first, last};
    }

}
