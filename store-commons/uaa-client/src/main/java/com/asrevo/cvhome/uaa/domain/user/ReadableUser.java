package com.asrevo.cvhome.uaa.domain.user;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * A uaa account as another service reads it.
 *
 * <p>
 * {@code org} and {@code store} are lifted out of uaa's metadata bag, which is where tenancy stamps them: a caller
 * asking "which organisation is this account in" should not have to know the bag's key names.
 * </p>
 *
 * <p>
 * {@code status} is uaa's derived state rather than a second boolean beside {@code active}: an account can be
 * enabled and still unable to sign in, because it is locked or was invited and never accepted. A caller that
 * branches on {@code active} alone will let a locked account through.
 * </p>
 */
@Getter
@Setter
public class ReadableUser extends UserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private String org;

    private String store;

    /** {@code ACTIVE}, {@code PENDING}, {@code LOCKED} or {@code DISABLED} — uaa derives it; nothing sets it. */
    private String status;

    /** Whether the address has been proven, by an accepted invitation or by an administrator. */
    private boolean emailVerified;

    /** Null for an account that has never signed in. */
    private Instant lastSignInAt;

    private Set<String> roles = new HashSet<>();

}
