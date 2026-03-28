package com.asrevo.cvhome.cua.security;

import com.asrevo.cvhome.cua.config.SocialProvider;
import com.asrevo.cvhome.cua.domain.User;
import com.asrevo.cvhome.cua.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		// We expect registrationId to be {clientId}.{providerId}
		String[] parts = registrationId.split("\\.", 2);
		String clientId = parts[0];
		String providerId = parts[1];

		SocialProvider provider = SocialProvider.fromProviderId(providerId);
		Map<String, Object> attributes = oAuth2User.getAttributes();

		String email;
		String name;

		if (provider != null) {
			email = provider.getEmail(attributes);
			name = provider.getName(attributes);
		}
		else {
			email = (String) attributes.get("email");
			name = (String) attributes.get("name");
			if (email == null) {
				email = (String) attributes.get("id");
			}
		}

		String finalEmail = email;
		User user = userRepository.findByClientIdAndEmail(clientId, finalEmail).orElseGet(() -> {
			User newUser = new User();
			newUser.setClientId(clientId);
			newUser.setEmail(finalEmail);
			newUser.setUsername(finalEmail); // for social login we use email as username
			if (name != null) {
				String[] nameParts = name.split(" ", 2);
				newUser.setFirstName(nameParts[0]);
				if (nameParts.length > 1) {
					newUser.setLastName(nameParts[1]);
				}
			}
			return userRepository.save(newUser);
		});

		return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes,
				userRequest.getClientRegistration()
					.getProviderDetails()
					.getUserInfoEndpoint()
					.getUserNameAttributeName());
	}

}
