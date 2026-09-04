package com.asrevo.cvhome.sso.domain;

import java.util.UUID;

/**
 * The identities uaa treats specially.
 */
public final class UaaConstants {

    /**
     * The seeded platform owner. Identified by id, not by email or username: both of those are editable facts about
     * the account, and a guard keyed on either would silently stop guarding the moment one changed.
     */
    public static final UUID SUPER_ADMIN_ID = UUID.fromString("65D8419C-8765-4B8B-A15F-910DCE959931");

    /** The one role the admin API never grants — it is what grants everything else. */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private UaaConstants() {
    }

}
