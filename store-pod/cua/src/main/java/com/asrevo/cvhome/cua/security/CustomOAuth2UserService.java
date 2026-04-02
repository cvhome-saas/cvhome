package com.asrevo.cvhome.cua.security;

import com.asrevo.cvhome.cua.domain.User;
import com.asrevo.cvhome.cua.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		return processOAuth2User(userRequest, oAuth2User);
	}

	public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// Extract clientId from registrationId (format: {clientId}.{providerId})
		String clientId;
		if (registrationId.contains(".")) {
			clientId = registrationId.substring(0, registrationId.lastIndexOf("."));
		}
		else {
			// Fallback if it doesn't follow the pattern (e.g. YAML configured ones might
			// not)
			// But for our dynamic ones it should.
			clientId = registrationId;
		}

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String email = (String) attributes.get("email");
		String name = (String) attributes.get("name");
		String firstName = (String) attributes.get("given_name");
		String lastName = (String) attributes.get("family_name");

		if (email == null) {
			log.warn("Email not found from OAuth2 provider for registrationId: {}", registrationId);
			// We might want to use another attribute as username if email is missing
			email = oAuth2User.getName();
		}

		Optional<User> userOptional = userRepository.findByClientIdAndEmail(clientId, email);
		User user;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			// Update existing user if needed
			updateExistingUser(user, firstName, lastName);
		}
		else {
			user = registerNewUser(clientId, email, firstName, lastName);
		}

		if (oAuth2User instanceof OidcUser oidcUser) {
			return new SecurityUser(user, attributes, oidcUser.getIdToken(), oidcUser.getUserInfo());
		}

		return new SecurityUser(user, attributes, null, null);
	}

	private User registerNewUser(String clientId, String email, String firstName, String lastName) {
		User user = new User();
		user.setClientId(clientId);
		user.setEmail(email);
		user.setUsername(email); // Use email as username for social login users
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEnabled(true);
		return userRepository.save(user);
	}

	private void updateExistingUser(User user, String firstName, String lastName) {
		boolean changed = false;
		if (firstName != null && !firstName.equals(user.getFirstName())) {
			user.setFirstName(firstName);
			changed = true;
		}
		if (lastName != null && !lastName.equals(user.getLastName())) {
			user.setLastName(lastName);
			changed = true;
		}
		if (changed) {
			userRepository.save(user);
		}
	}

}
