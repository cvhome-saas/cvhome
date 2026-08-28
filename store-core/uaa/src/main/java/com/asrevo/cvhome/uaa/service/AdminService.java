package com.asrevo.cvhome.uaa.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;
import com.asrevo.cvhome.uaa.repo.RoleRepository;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import com.asrevo.cvhome.uaa.repo.UserSpecifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Map<String, String> metadataFilters, Pageable pageable) {
        Specification<User> spec = buildSpec(metadataFilters);
        Page<User> all = userRepository.findAll(spec, pageable);
        return all.map(u -> new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.isEnabled(), u.getRoles().stream().map(Role::getName).collect(toSet()), u.getMetadata()));
    }

    private Specification<User> buildSpec(Map<String, String> metadataFilters) {
        Specification<User> spec = (_, _, cb) -> cb.conjunction();
        if (metadataFilters != null) {
            for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    spec = spec.and(UserSpecifications.hasMetadataField(entry.getKey(), entry.getValue()));
                }
            }
        }
        return spec;
    }

    // orElseThrow is generic over the thrown type, so a checked exception needs no Unchecked wrapper here.
    private User findUser(UUID id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> UserNotFoundException.of(id));
    }

    public UserDto getUser(UUID id) throws UserNotFoundException {
        User u = findUser(id);
        return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName(), u.isEnabled(),
                u.getRoles().stream().map(Role::getName).collect(toSet()), u.getMetadata());
    }

    @Transactional
    // Declares both because it delegates to assignRoles and getUser. Neither can fire in practice — the user was
    // created a line earlier and is not the super admin — but the signature states what the code path can produce
    // rather than what the author expects, which is what keeps it true when the delegates change.
    public UserDto createUser(CreateUserRequest req) throws UserNotFoundException, SuperAdminImmutableException {
        String username = req.username();
        String email = req.email();
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        if (req.metadata() != null) {
            u.getMetadata().putAll(req.metadata());
        }
        User saved = userRepository.save(u);
        if (req.roles() != null && !req.roles().isEmpty()) {
            assignRoles(saved.getId(), req.roles());
        }
        return getUser(saved.getId());
    }

    @Transactional
    public void assignRoles(UUID id, Set<String> roleNames) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        var assignableRoles = getAssignableRoles();
        if (roleNames != null) {
            for (String rn : roleNames) {
                if (!assignableRoles.contains(rn)) {
                    continue;
                }
                Role role = roleRepository.findByName(rn).orElseGet(() -> roleRepository.save(new Role(rn)));
                u.getRoles().add(role);
            }
        }
    }

    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest req) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        if (req.firstName() != null) {
            u.setFirstName(req.firstName());
        }
        if (req.lastName() != null) {
            u.setLastName(req.lastName());
        }
        if (req.enabled() != null) {
            u.setEnabled(req.enabled());
        }
        if (req.metadata() != null) {
            u.getMetadata().putAll(req.metadata());
        }
        if (req.roles() != null) {
            u.getRoles().clear();
            assignRoles(u.getId(), req.roles());
        }
        User saved = userRepository.save(u);
        return getUser(saved.getId());
    }

    @Transactional
    public void removeRoles(UUID id, Set<String> roleNames) throws UserNotFoundException, SuperAdminImmutableException {
        if (roleNames == null || roleNames.isEmpty()) {
            return;
        }
        User u = getNonSuperAdmin(id);
        u.getRoles().removeIf(r -> roleNames.contains(r.getName()));
    }

    @Transactional
    public void resetPassword(UUID userId, ResetUserPasswordRequest req) throws UserNotFoundException {
        User u = findUser(userId);
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public boolean usernameExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public void enableUser(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        u.setEnabled(true);
    }

    @Transactional
    public void disableUser(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        u.setEnabled(false);
    }

    @Transactional
    public void delete(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        userRepository.deleteById(u.getId());
    }

    private User getNonSuperAdmin(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User user = findUser(id);
        if ("super-admin@mail.com".equals(user.getEmail())) {
            throw SuperAdminImmutableException.of(id);
        }
        return user;
    }

    public Set<String> getAssignableRoles() {
        return roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .filter(roleName -> !roleName.equals("SUPER_ADMIN"))
                .collect(toSet());
    }

}
