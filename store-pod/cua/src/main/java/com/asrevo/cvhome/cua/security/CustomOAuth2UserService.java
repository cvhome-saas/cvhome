package com.asrevo.cvhome.cua.security;

import com.asrevo.cvhome.cua.config.SocialProvider;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.domain.User;
import com.asrevo.cvhome.cua.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

		SocialLoginConfigId socialLoginConfigId = SocialLoginConfigId.fromRegistrationId(registrationId);

		SocialProvider provider = socialLoginConfigId.providerId();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		SocialProvider.ExtractedAttributes extractedAttributes = provider.extractAttributes(attributes);

		String clientId = socialLoginConfigId.storeMerchantId().storeMerchantId();

		var user = userRepository.findByClientIdAndEmail(clientId, extractedAttributes.email())
			.orElseGet(() -> registerNewUser(socialLoginConfigId, extractedAttributes));

		return new SecurityUser(user);
	}

	private User registerNewUser(SocialLoginConfigId socialLoginConfigId,
			SocialProvider.ExtractedAttributes extractedAttributes) {

		User user = new User();
		user.setClientId(socialLoginConfigId.storeMerchantId().storeMerchantId());
		user.setEmail(extractedAttributes.email());
		user.setUsername(extractedAttributes.name());
		user.setFirstName(extractedAttributes.firstname());
		user.setLastName(extractedAttributes.lastname());
		user.setEnabled(true);
		return userRepository.save(user);

	}

}
