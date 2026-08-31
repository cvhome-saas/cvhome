package com.asrevo.cvhome.tenancy.manager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * What a caller sends to create an organization and its first administrator.
 *
 * <p>
 * The wire shape is unchanged — {@code {"user": {...}}}, which is what the console's sign-up form and the
 * super-admin create dialog both post. What changed is the type inside it: {@link SignUpUser} instead of uaa's
 * {@code PersistableUser}, so the request can carry only what a signup may state. See {@link SignUpUser} for why
 * that distinction is a security boundary and not a tidy-up.
 * </p>
 *
 * <p>
 * {@code @Valid} on the component is what makes the nested constraints run at all, and it is also what gives the
 * console's {@code fieldErrors[]} paths their {@code user.} prefix — {@code user.emailAddress} resolves to the
 * control that caused it because the form group is nested to match.
 * </p>
 */
public record CreateOrgRequest(@NotNull @Valid SignUpUser user) {
}
