package com.asrevo.cvhome.cua.web;

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

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

	private final RequestCache requestCache;

	@GetMapping("/login")
	public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		if (savedRequest != null) {
			String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
				.build()
				.getQueryParams()
				.getFirst("client_id");
			Optional.ofNullable(clientId).ifPresent(it -> model.addAttribute("clientId", it));
		}
		return "login";
	}

}
// http://org1-store1.spg-507f1f77.gateway.com:80/cua/oauth2/authorize?response_type=code&client_id=web-app&scope=openid&state=wluMmDBnDBX9ukT3ngWhC2vSTrhAZu10pGBXOtcZ9Qo%3D&redirect_uri=http://org1-store1.spg-507f1f77.gateway.com/callback&nonce=U6H2w96cGF3jSEpaCNNtKpkYgUWRAhGf3Wuu0W536oc&code_challenge=MOySr8hcWy7BPgvQDzx5UvuUZq-kipC298xOPA-e6r8&code_challenge_method=S256