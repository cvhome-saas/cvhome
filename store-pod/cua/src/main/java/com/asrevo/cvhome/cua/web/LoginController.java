package com.asrevo.cvhome.cua.web;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
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
import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

	private final RequestCache requestCache;

	private final SocialLoginConfigRepository socialLoginConfigRepository;

	private final ExternalMerchantStoreService externalMerchantStoreService;

	@GetMapping("/login")
	public String login(HttpServletRequest request, Locale locale, HttpServletResponse response, Model model) {
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		if (savedRequest != null) {
			String clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
				.build()
				.getQueryParams()
				.getFirst("client_id");
			if (clientId != null) {

				ReadableMerchantStore store = externalMerchantStoreService.getStore(new StoreMerchantId(clientId));
				model.addAttribute("store", store);
				model.addAttribute("clientId", clientId);
				List<SocialLoginConfigId> configs = socialLoginConfigRepository
					.findEnabledSocialLoginConfig(new StoreMerchantId(clientId));
				model.addAttribute("socialLogins", configs);
			}
		}
		return "login";
	}

}