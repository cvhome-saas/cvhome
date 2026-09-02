package com.asrevo.cvhome.cua.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.User;
import com.asrevo.cvhome.cua.dto.ReadableUser;
import com.asrevo.cvhome.cua.errors.DuplicateEmailException;
import com.asrevo.cvhome.cua.errors.DuplicateUsernameException;
import com.asrevo.cvhome.cua.repo.UserRepository;
import com.asrevo.cvhome.cua.web.dto.RegistrationRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a shopper account for {@code store}. The store is the caller's to supply, never the request body's:
     * a shopper registers with the store whose page they are on, and letting the body name the store would let one
     * form register accounts anywhere on the pod.
     */
    @Transactional
    public void registerUser(StoreMerchantId store, RegistrationRequest request)
            throws DuplicateUsernameException, DuplicateEmailException {
        String clientId = store.getId();
        if (userRepository.findByClientIdAndUsername(clientId, request.getUsername()).isPresent()) {
            throw DuplicateUsernameException.of(clientId, request.getUsername());
        }

        if (userRepository.findByClientIdAndEmail(clientId, request.getEmail()).isPresent()) {
            throw DuplicateEmailException.of(clientId, request.getEmail());
        }

        User user = new User();
        user.setClientId(clientId);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        userRepository.save(user);
    }

    public Optional<ReadableUser> getById(UUID id) {
        return userRepository.findById(id).map(ReadableUser::fromUser);
    }

}
