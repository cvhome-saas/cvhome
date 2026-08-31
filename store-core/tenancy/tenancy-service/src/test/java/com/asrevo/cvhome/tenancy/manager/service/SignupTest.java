package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.errors.DuplicateSignupEmailException;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.tenancy.manager.dto.SignUpUser;
import com.asrevo.cvhome.tenancy.manager.service.impl.SignupServiceImpl;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Public signup — the one endpoint on the platform anyone on the internet may call, and the one that creates a
 * tenant.
 *
 * <p>
 * Everything below was reachable before this suite existed. The endpoint had no {@code @Valid}, so it accepted
 * empty names, {@code not-an-email} as an address, a one-character password and a mismatched confirmation, all with
 * a 200; it took uaa's {@code PersistableUser} straight off the wire, so a stranger could name a {@code store};
 * a duplicate address came back as a code that also means six other things; and a failure after uaa had said yes
 * left an account nobody could use and nobody could replace.
 * </p>
 */
class SignupTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("65f023632bc46470c104b76f");

    private static final String USER_ID = "c0ffee00-dead-4bee-8000-000000000001";

    private static final String EMAIL = "ada@example.com";

    private static final String PASSWORD = "correct-horse-8";

    private static final String FIRST_NAME = "Ada";

    private static final String LAST_NAME = "Lovelace";

    private static final String ORG_NAME = "Nordwerk";

    private static final String EMAIL_FIELD = "emailAddress";

    private static final String PASSWORD_FIELD = "password";

    /** Kept as typed, spaces and all — the assertion that normalisation leaves a password alone. */
    private static final String PADDED_PASSWORD = " pass word 8 ";

    private static ValidatorFactory factory;

    private static Validator validator;

    private UserAccountService users;

    private InternalOrgService orgs;

    private static SignUpUser valid() {
        return new SignUpUser(FIRST_NAME, LAST_NAME, EMAIL, ORG_NAME, PASSWORD, PASSWORD);
    }

    private static ReadableUser created() {
        ReadableUser user = new ReadableUser();
        user.setId(USER_ID);
        user.setEmailAddress(EMAIL);
        return user;
    }

    /** The field paths a violation is reported against — the console binds its form controls by exactly these. */
    private static Set<String> pathsOf(SignUpUser user) {
        return validator.validate(user).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(String::valueOf)
                .collect(Collectors.toSet());
    }

    @BeforeEach
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        users = mock(UserAccountService.class);
        orgs = mock(InternalOrgService.class);
    }

    @AfterEach
    void tearDown() {
        factory.close();
        TransactionSynchronizationManager.clear();
    }

    /* ----------------------------------------------------------------------- validation ---- */

    @Test
    @DisplayName("a well-formed signup has nothing to complain about")
    void aValidSignupPasses() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @ParameterizedTest(name = "{3}")
    @CsvSource(nullValues = "null", value = {
        "'   ',Lovelace,firstName,a name of spaces is not a name",
        "Ada,'',lastName,an empty family name is refused",
        "null,Lovelace,firstName,a missing name is refused",
    })
    void namesMustBePresent(String first, String last, String field, String description) {
        assertThat(pathsOf(new SignUpUser(first, last, EMAIL, null, PASSWORD, PASSWORD))).contains(field);
    }

    @Test
    @DisplayName("an address that is not one is refused, and so is one the organization's column cannot hold")
    void theAddressIsCheckedAndBounded() {
        assertThat(pathsOf(new SignUpUser(FIRST_NAME, LAST_NAME, "not-an-email", null, PASSWORD, PASSWORD)))
                .contains(EMAIL_FIELD);

        // 50 is tenancy.manager_org.email, not uaa's varchar(254): the organization is inserted first, so its
        // column is the binding one. Past it the insert used to fail as a bare 409 that the console read as
        // "already registered" — a message that was not merely unhelpful but false.
        String tooLong = String.format("%s@example.com", "a".repeat(45));
        assertThat(tooLong).hasSizeGreaterThan(SignUpUser.MAX_EMAIL_LENGTH);
        assertThat(pathsOf(new SignUpUser(FIRST_NAME, LAST_NAME, tooLong, null, PASSWORD, PASSWORD)))
                .contains(EMAIL_FIELD);
    }

    @Test
    @DisplayName("the confirmation is compared, and the mismatch lands on the field that should change")
    void thePasswordsMustMatch() {
        // It was read by nothing: password "a" with repeatPassword "b" was accepted and the account created.
        assertThat(pathsOf(new SignUpUser(FIRST_NAME, LAST_NAME, EMAIL, null, PASSWORD, "something-else-9")))
                .contains("repeatPassword");
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource({
        "short1,a password under eight characters is refused",
        "password123,one of the passwords tried first is refused",
        "ada-lovelace-1,a password containing the name typed above it is refused",
    })
    void weakPasswordsAreRefused(String password, String description) {
        assertThat(pathsOf(new SignUpUser(FIRST_NAME, LAST_NAME, EMAIL, null, password, password)))
                .contains(PASSWORD_FIELD);
    }

    @Test
    @DisplayName("a password past bcrypt's 72 bytes is refused rather than silently truncated")
    void anOverLongPasswordIsRefused() {
        String password = "z".repeat(SignUpUser.MAX_PASSWORD_LENGTH + 1);
        assertThat(pathsOf(new SignUpUser(FIRST_NAME, LAST_NAME, EMAIL, null, password, password)))
                .contains(PASSWORD_FIELD);
    }

    /* -------------------------------------------------------------------- normalisation ---- */

    @Test
    @DisplayName("the address is lowercased and the names trimmed; the password is left exactly as typed")
    void valuesAreNormalisedBeforeTheyAreStored() {
        SignUpUser normalized =
                new SignUpUser("  Ada ", " Lovelace ", "  Ada@Example.COM ", " Nordwerk ", PADDED_PASSWORD, " x ")
                        .normalized();

        // uaa's unique constraint is case-sensitive, so without this Ada@ and ada@ are two accounts that every
        // human involved believes to be one.
        assertThat(normalized.emailAddress()).isEqualTo(EMAIL);
        assertThat(normalized.firstName()).isEqualTo(FIRST_NAME);
        assertThat(normalized.organizationName()).isEqualTo(ORG_NAME);
        // A space is a character like any other in a password; trimming one creates an account with a secret its
        // owner cannot reproduce.
        assertThat(normalized.password()).isEqualTo(PADDED_PASSWORD);
    }

    @Test
    @DisplayName("an organization with no name given is named after its founder, cut to the column")
    void theOrganizationIsAlwaysNamed() {
        assertThat(new SignUpUser(FIRST_NAME, LAST_NAME, EMAIL, null, PASSWORD, PASSWORD).organizationNameOrDefault())
                .isEqualTo("Ada Lovelace");

        // Two names of 50 plus a space is 101, one past manager_org.name. Refusing a signup over the fallback for
        // a field the visitor never filled in would be absurd, so it is cut instead.
        String fifty = "n".repeat(SignUpUser.MAX_NAME_LENGTH);
        assertThat(new SignUpUser(fifty, fifty, EMAIL, null, PASSWORD, PASSWORD).organizationNameOrDefault())
                .hasSize(SignUpUser.MAX_ORG_NAME_LENGTH);
    }

    /* -------------------------------------------------------------------------- the flow ---- */

    @Test
    @DisplayName("the administrator is created with the role and org signup decides, and nothing a caller sent")
    void signupDecidesThePrivilegedFields() throws Exception {
        when(orgs.createOrgForUser(any(), any())).thenReturn(ORG);
        when(users.createUser(any())).thenReturn(created());

        service().createOrgUser(new CreateOrgRequest(valid()));

        ArgumentCaptor<PersistableUser> sent = ArgumentCaptor.forClass(PersistableUser.class);
        verify(users).createUser(sent.capture());
        assertThat(sent.getValue().getRoles()).containsExactly("ORG_ADMIN");
        assertThat(sent.getValue().getOrg()).isEqualTo(ORG.id().toString());
        assertThat(sent.getValue().isActive()).isTrue();
        // The field the old code forgot to overwrite: a public body naming somebody else's store id used to reach
        // uaa's metadata, which is what the store-scoped permission checks read. SignUpUser cannot express it.
        assertThat(sent.getValue().getStore()).isNull();
        assertThat(sent.getValue().getUserName()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("the organization is created with the name the signup gave it")
    void theOrganizationIsCreatedNamed() throws Exception {
        when(orgs.createOrgForUser(any(), any())).thenReturn(ORG);
        when(users.createUser(any())).thenReturn(created());

        service().createOrgUser(new CreateOrgRequest(valid()));

        verify(orgs).createOrgForUser(new Email(EMAIL), ORG_NAME);
    }

    @Test
    @DisplayName("a taken address is its own conflict, with a field error, not uaa's generic one")
    void aTakenAddressIsNamed() throws Exception {
        when(orgs.createOrgForUser(any(), any())).thenReturn(ORG);
        when(users.createUser(any())).thenThrow(UaaConflictException.class);

        assertThatThrownBy(() -> service().createOrgUser(new CreateOrgRequest(valid())))
                .isInstanceOf(DuplicateSignupEmailException.class)
                .satisfies(e -> {
                    DuplicateSignupEmailException conflict = (DuplicateSignupEmailException) e;
                    // COMMON.DATA_INTEGRITY_VIOLATION also means six other things, which is why the console had to
                    // guess that any fieldless 409 was a taken address.
                    assertThat(conflict.errorCode().code()).isEqualTo("CONTROL_PLANE.SIGNUP.EMAIL_TAKEN");
                    assertThat(conflict.fieldErrors()).singleElement()
                            .satisfies(it -> assertThat(it.field()).isEqualTo("user.emailAddress"));
                });
    }

    @Test
    @DisplayName("uaa being unreachable is left undecided rather than reported as a duplicate")
    void anUnreachableUaaIsNotAConflict() throws Exception {
        when(orgs.createOrgForUser(any(), any())).thenReturn(ORG);
        when(users.createUser(any())).thenThrow(UaaApiUnavailableException.class);

        assertThatThrownBy(() -> service().createOrgUser(new CreateOrgRequest(valid())))
                .isInstanceOf(UaaApiUnavailableException.class);
        verify(orgs, never()).recordOwner(any(), any());
    }

    /* ---------------------------------------------------------------------- compensation ---- */

    @Test
    @DisplayName("a rollback after uaa said yes deletes the account it created")
    void aRollbackTakesTheUaaUserWithIt() throws Exception {
        when(orgs.createOrgForUser(any(), any())).thenReturn(ORG);
        when(users.createUser(any())).thenReturn(created());
        TransactionSynchronizationManager.initSynchronization();

        service().createOrgUser(new CreateOrgRequest(valid()));

        // uaa is a network hop with its own database, so it is not in this transaction: without the compensation
        // a failed commit leaves an account whose address its owner can never sign up with again.
        completeWith(TransactionSynchronization.STATUS_ROLLED_BACK);
        verify(users).deleteUser(USER_ID);
    }

    @Test
    @DisplayName("a commit leaves the account alone")
    void aCommitKeepsTheUser() throws Exception {
        when(orgs.createOrgForUser(any(), any())).thenReturn(ORG);
        when(users.createUser(any())).thenReturn(created());
        TransactionSynchronizationManager.initSynchronization();

        service().createOrgUser(new CreateOrgRequest(valid()));

        completeWith(TransactionSynchronization.STATUS_COMMITTED);
        verify(users, never()).deleteUser(any());
    }

    private static void completeWith(int status) {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(status));
    }

    private SignupServiceImpl service() {
        return new SignupServiceImpl(users, orgs);
    }

}
