package com.asrevo.cvhome.uaa.domain;

/**
 * Where a role means something.
 *
 * <p>
 * {@code REALM}: across the whole platform, every client. {@code ORGANIZATION}: inside one seller organization and
 * its stores — the {@code org}/{@code store} claims say which. {@code CLIENT}: only issued when authenticating
 * through a specific client. Informational today; services still key on the role name.
 * </p>
 */
public enum RoleScope {
    REALM, ORGANIZATION, CLIENT
}
