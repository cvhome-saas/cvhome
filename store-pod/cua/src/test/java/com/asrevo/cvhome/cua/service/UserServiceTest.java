package com.asrevo.cvhome.cua.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.User;
import com.asrevo.cvhome.cua.errors.CuaErrors;
import com.asrevo.cvhome.cua.errors.DuplicateEmailException;
import com.asrevo.cvhome.cua.errors.DuplicateUsernameException;
import com.asrevo.cvhome.cua.repo.UserRepository;
import com.asrevo.cvhome.cua.web.dto.RegistrationRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String USERNAME = "jane";

    private static final String EMAIL = "jane@example.com";

    private static final String PASSWORD = "secret-1";

    private static final String HASH = "{bcrypt}hash";

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    private static RegistrationRequest request() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(USERNAME);
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        return request;
    }

    @Test
    void aNewShopperIsStampedWithTheStoreAndAnEncodedPassword()
            throws DuplicateUsernameException, DuplicateEmailException {
        when(users.findByClientIdAndUsername(STORE.getId(), USERNAME)).thenReturn(Optional.empty());
        when(users.findByClientIdAndEmail(STORE.getId(), EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);

        service.registerUser(STORE, request());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getClientId()).isEqualTo(STORE.getId());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo(HASH);
        assertThat(saved.getValue().isEnabled()).isTrue();
    }

    @Test
    void aTakenUsernameIsTheUsernameConflict() {
        when(users.findByClientIdAndUsername(STORE.getId(), USERNAME)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.registerUser(STORE, request()))
                .isInstanceOf(DuplicateUsernameException.class)
                .extracting(it -> ((DuplicateUsernameException) it).payload().errorCode())
                .isEqualTo(CuaErrors.USERNAME_TAKEN);
        verify(users, never()).save(any());
    }

    @Test
    void aTakenEmailIsTheEmailConflict() {
        when(users.findByClientIdAndUsername(anyString(), anyString())).thenReturn(Optional.empty());
        when(users.findByClientIdAndEmail(STORE.getId(), EMAIL)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.registerUser(STORE, request()))
                .isInstanceOf(DuplicateEmailException.class);
        verify(users, never()).save(any());
    }

}
