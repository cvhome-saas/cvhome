package com.asrevo.cvhome.tenancy.manager.dto;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.tenancy.manager.validation.PasswordsMatch;
import com.asrevo.cvhome.tenancy.manager.validation.StrongPassword;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Everything a signup may state about the administrator it is creating — and nothing else.
 *
 * <h2>Why this exists rather than {@code PersistableUser}</h2>
 *
 * <p>
 * {@code CreateOrgRequest} used to carry uaa's own {@code PersistableUser}, which is the type the platform
 * <em>writes</em> a user with: it has {@code roles}, {@code org}, {@code store}, {@code active} and {@code id}.
 * Public signup is the one endpoint anyone on the internet may call, so every one of those was an input a stranger
 * could set. {@code SignupServiceImpl} overwrote four of them on the way past — but not {@code store}, so a body
 * naming somebody else's store id stamped that id into the new account's uaa metadata, which is the value the
 * store-scoped permission checks read. A request type that cannot express the field is a better fix than another
 * overwrite, because it also cannot be forgotten when a field is added to uaa's model.
 * </p>
 *
 * <h2>The limits are the columns'</h2>
 *
 * <p>
 * Each maximum below is read off the column that stores the value, not chosen:
 * </p>
 *
 * <ul>
 * <li>{@code firstName} / {@code lastName} — {@code uaa.users.first_name} / {@code last_name}, {@code varchar(50)}.</li>
 * <li>{@code emailAddress} — <strong>50</strong>, from {@code tenancy.manager_org.email}, not uaa's roomier
 * {@code varchar(254)}: {@code SignupServiceImpl} inserts the organization first, so tenancy's own column is the
 * binding one. Past it the insert fails with a {@code DataIntegrityViolationException} that the shared advice can
 * only render as a bare {@code 409 COMMON.DATA_INTEGRITY_VIOLATION} — indistinguishable, to the console, from a
 * duplicate address, which is how an over-long address used to be reported as "already registered".</li>
 * <li>{@code organizationName} — {@code tenancy.manager_org.name}, {@code varchar(100)}.</li>
 * <li>{@code password} — 72, where bcrypt stops reading. uaa hashes with
 * {@code PasswordEncoderFactories.createDelegatingPasswordEncoder()}, so anything past that is silently ignored
 * rather than stored, and a form that accepted it would let someone believe in characters that protect nothing.</li>
 * </ul>
 *
 * <p>
 * The bean-validation messages are English defaults on purpose. The console renders a field error by its
 * {@code VALIDATION.<Constraint>} code and falls back to this text only when it has no translation, so the message
 * is a legible last resort rather than the primary channel.
 * </p>
 *
 * <p>
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} keeps the wire shape backwards compatible: the console and
 * seller-ui both post the full {@code PersistableUser} shape, and a request that merely mentions {@code roles} should
 * be answered by ignoring the field, not by a 400 that names a field this endpoint has no opinion about.
 * </p>
 *
 * @param firstName        the administrator's given name
 * @param lastName         the administrator's family name
 * @param emailAddress     the address that becomes both the uaa username and the organization's contact
 * @param organizationName what to call the organization; optional, and defaulted from the name when absent
 * @param password         the first administrator's password
 * @param repeatPassword   the confirmation, compared by {@link PasswordsMatch}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@PasswordsMatch
@StrongPassword
public record SignUpUser(

        @NotBlank @Size(max = SignUpUser.MAX_NAME_LENGTH, message = "must be at most 50 characters")
        String firstName,

        @NotBlank @Size(max = SignUpUser.MAX_NAME_LENGTH, message = "must be at most 50 characters")
        String lastName,

        @NotBlank @Email @Size(max = SignUpUser.MAX_EMAIL_LENGTH, message = "must be at most 50 characters")
        String emailAddress,

        @Size(max = SignUpUser.MAX_ORG_NAME_LENGTH, message = "must be at most 100 characters")
        String organizationName,

        @NotBlank
        @Size(min = SignUpUser.MIN_PASSWORD_LENGTH, max = SignUpUser.MAX_PASSWORD_LENGTH,
                message = "must be between 8 and 72 characters")
        String password,

        @NotBlank
        String repeatPassword) {

    /** {@code uaa.users.first_name} and {@code last_name} are both {@code varchar(50)}. */
    public static final int MAX_NAME_LENGTH = 50;

    /** {@code tenancy.manager_org.email} — the narrowest of the three columns this address is written to. */
    public static final int MAX_EMAIL_LENGTH = 50;

    /** {@code tenancy.manager_org.name}. */
    public static final int MAX_ORG_NAME_LENGTH = 100;

    /**
     * The shortest password the platform will create an organization owner with.
     *
     * <p>
     * Deliberately a length rule and not a composition rule. Composition rules ("one upper, one digit, one symbol")
     * produce {@code Password1!}, which {@link StrongPassword} rejects by name, and NIST has advised against them
     * since SP 800-63B. Length plus a screen for the passwords tried first is the rule that survives real users.
     * </p>
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** Where bcrypt stops reading. Beyond this a password is not stronger, it is truncated. */
    public static final int MAX_PASSWORD_LENGTH = 72;

    /**
     * The same values with the whitespace gone, ready to be stored.
     *
     * <p>
     * The address is lowercased as well as trimmed: it is the uaa username, and uaa's unique constraint is
     * case-sensitive, so {@code Ada@example.com} and {@code ada@example.com} would otherwise be two accounts that
     * every human involved believes to be one. The two passwords are left exactly as typed — a space is a character
     * like any other in a password, and quietly changing one creates an account with a secret its owner cannot
     * reproduce.
     * </p>
     *
     * <p>
     * Normalising <em>after</em> validation rather than before is intentional. {@code @NotBlank} already rejects a
     * name of spaces, so trimming first would only hide which value was posted; here it simply prevents storing the
     * padding around a name that is otherwise fine.
     * </p>
     */
    public SignUpUser normalized() {
        return new SignUpUser(trimmed(firstName), trimmed(lastName), trimmed(emailAddress).toLowerCase(Locale.ROOT),
                trimmed(organizationName), password, repeatPassword);
    }

    /**
     * What to call the organization: what was asked for, or the administrator's own name.
     *
     * <p>
     * {@code manager_org.name} is nullable and {@code createOrgFromUser} never set it, so the column was null for
     * every organization on the platform and the console's list screen falls back to showing the contact email.
     * A signup knows a name — worst case the founder's — and an organization that has one from the first second is
     * one less thing an operator has to fix by hand.
     * </p>
     *
     * <p>
     * The result is truncated to the column rather than validated against it: {@code organizationName} is checked at
     * 100 and refused past it, but the <em>default</em> is two names of up to 50 plus a space, which is 101 in the
     * worst case. Refusing a signup because the fallback for a field the visitor did not fill in is one character
     * too long would be absurd, so the fallback is cut to fit.
     * </p>
     */
    public String organizationNameOrDefault() {
        String named = trimmed(organizationName);
        String result = named.isEmpty() ? String.format("%s %s", trimmed(firstName), trimmed(lastName)).strip() : named;
        return result.length() <= MAX_ORG_NAME_LENGTH ? result : result.substring(0, MAX_ORG_NAME_LENGTH);
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.strip();
    }

}
