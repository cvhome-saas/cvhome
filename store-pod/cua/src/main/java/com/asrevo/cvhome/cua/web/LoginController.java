package com.asrevo.cvhome.cua.web;

import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

	private final RequestCache requestCache;

	private final SocialLoginConfigRepository socialLoginConfigRepository;

	@GetMapping("/login")
	public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		if (savedRequest != null) {
			String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
				.build()
				.getQueryParams()
				.getFirst("client_id");
			if (clientId != null) {
				model.addAttribute("clientId", clientId);
				List<SocialLoginConfig> configs = socialLoginConfigRepository.findAllByClientId(clientId);
				model.addAttribute("socialLogins", configs.stream()
					.map(config -> new SocialLoginInfo(config.getProviderId(), clientId + "." + config.getProviderId()))
					.collect(Collectors.toList()));
			}
		}
		return "login";
	}

	public record SocialLoginInfo(String providerId, String registrationId) {
	}

}