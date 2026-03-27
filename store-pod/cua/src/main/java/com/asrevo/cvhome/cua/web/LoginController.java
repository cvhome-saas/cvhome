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