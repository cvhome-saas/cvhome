package com.asrevo.cvhome.uaa.service;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.repo.RoleRepository;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.repo.UserSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
		Specification<User> spec = (root, query, cb) -> cb.conjunction();
		if (metadataFilters != null) {
			for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					spec = spec.and(UserSpecifications.hasMetadataField(entry.getKey(), entry.getValue()));
				}
			}
		}
		Page<User> all = userRepository.findAll(spec, pageable);
		return all.map(u -> new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getStatus(),
				u.getRoles().stream().map(Role::getName).collect(toSet()), u.getMetadata()));
	}

	public UserDto getUser(UUID id) {
		return userRepository.findById(id)
			.map(u -> new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getStatus(),
					u.getRoles().stream().map(Role::getName).collect(toSet()), u.getMetadata()))
			.orElseThrow(() -> new RuntimeException("Invalid user id " + id));
	}

	@Transactional
	public UserDto createUser(CreateUserRequest req) {
		String username = req.username();
		String email = req.email();
		User u = new User();
		u.setUsername(username);
		u.setEmail(email);
		if (req.metadata() != null) {
			u.getMetadata().putAll(req.metadata());
		}
		User saved = userRepository.save(u);
		return new UserDto(saved.getId(), saved.getUsername(), saved.getEmail(), u.getStatus(),
				saved.getRoles().stream().map(Role::getName).collect(toSet()), saved.getMetadata());

	}

	@Transactional
	public void assignRoles(UUID userId, Set<String> roleNames) {
		User u = userRepository.findById(userId).orElseThrow();
		Set<Role> rs = new HashSet<>();
		for (String rn : roleNames) {
			rs.add(roleRepository.findByName(rn).orElseGet(() -> roleRepository.save(new Role(rn))));
		}
		u.getRoles().clear();
		u.getRoles().addAll(rs);
	}

	@Transactional
	public UserDto updateUser(UUID userId, UpdateUserRequest req) {
		User u = userRepository.findById(userId).orElseThrow();
		if (req.status() != null && !req.status().isBlank())
			u.setStatus(req.status());
		if (req.metadata() != null) {
			u.getMetadata().putAll(req.metadata());
		}
		User saved = userRepository.save(u);
		return new UserDto(saved.getId(), saved.getUsername(), saved.getEmail(), u.getStatus(),
				saved.getRoles().stream().map(Role::getName).collect(toSet()), saved.getMetadata());
	}

	@Transactional
	public void removeRoles(UUID userId, Set<String> roleNames) {
		if (roleNames == null || roleNames.isEmpty())
			return;
		User u = userRepository.findById(userId).orElseThrow();
		u.getRoles().removeIf(r -> roleNames.contains(r.getName()));
	}

	@Transactional
	public void resetPassword(UUID userId, ResetUserPasswordRequest req) {
		User u = userRepository.findById(userId).orElseThrow();
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
	public void enableUser(UUID id) {
		User u = userRepository.findById(id).orElseThrow();
		u.setStatus("ACTIVE");
		userRepository.save(u);
	}

	@Transactional
	public void disableUser(UUID id) {
		User u = userRepository.findById(id).orElseThrow();
		u.setStatus("DISABLED");
		userRepository.save(u);
	}

	@Transactional
	public void delete(UUID id) {
		User user = userRepository.findById(id).orElseThrow();
		if ("super-admin@mail.com".equals(user.getEmail()))
			throw new RuntimeException("Cannot delete admin user");
		userRepository.deleteById(id);
	}

}
