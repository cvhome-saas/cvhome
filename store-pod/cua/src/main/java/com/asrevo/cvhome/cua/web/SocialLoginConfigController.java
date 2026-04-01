package com.asrevo.cvhome.cua.web;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.config.SocialProvider;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.service.SocialLoginConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/private/social-login-config")
@RequiredArgsConstructor
public class SocialLoginConfigController {

	private final SocialLoginConfigService service;

	@GetMapping
	public List<SocialLoginConfig> getConfigs(@SecuredResource StoreMerchantId merchantStore) {
		return service.getConfigs(merchantStore);
	}

	@PostMapping
	public void saveConfigs(@SecuredResource StoreMerchantId merchantStore,
			@RequestBody List<SocialLoginConfig> configs) {
		service.saveConfigs(merchantStore, configs);
	}

	@GetMapping("/supported-social-providers")
	public SocialProvider[] getSupportedProviders() {
		return SocialProvider.values();
	}

}
