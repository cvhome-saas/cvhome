package com.asrevo.cvhome.cua.service;

import com.asrevo.cvhome.cua.domain.User;
import com.asrevo.cvhome.cua.repo.UserRepository;
import com.asrevo.cvhome.cua.web.dto.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void registerUser(RegistrationRequest request) {
		if (userRepository.findByClientIdAndUsername(request.getClientId(), request.getUsername()).isPresent()) {
			throw new IllegalArgumentException("Username already exists for this client");
		}

		if (userRepository.findByClientIdAndEmail(request.getClientId(), request.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Email already exists for this client");
		}

		User user = new User();
		user.setClientId(request.getClientId());
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setEnabled(true);

		userRepository.save(user);
	}

}
