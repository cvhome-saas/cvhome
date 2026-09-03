package com.asrevo.cvhome.uaa.idp;

import java.util.Map;

/** The strings the presets share. An enum constant cannot read its own enum's static fields, hence a holder. */
final class IdpDefaults {

    static final String OIDC_SCOPES = "openid profile email";

    static final String SUB = "sub";

    static final String ID = "id";

    static final String SECRET_BASIC = "client_secret_basic";

    static final String SECRET_POST = "client_secret_post";

    static final String EMAIL = "email";

    static final String FIRST_NAME = "firstName";

    static final String LAST_NAME = "lastName";

    static final String GIVEN_NAME = "given_name";

    static final String FAMILY_NAME = "family_name";

    static final String NAME = "name";

    static final Map<String, String> OIDC_MAPPING = Map.of(EMAIL, EMAIL, GIVEN_NAME, FIRST_NAME, FAMILY_NAME, LAST_NAME);

    static final Map<String, String> NAME_MAPPING = Map.of(EMAIL, EMAIL, NAME, FIRST_NAME);

    private IdpDefaults() {
    }

}
