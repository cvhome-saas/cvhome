package com.asrevo.cvhome.cua.web;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.config.SocialProvider;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.service.SocialLoginConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/private/social-login-config")
@RequiredArgsConstructor
public class SocialLoginConfigController {

	private final SocialLoginConfigService service;

	@GetMapping
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUA.*')")
	public List<SocialLoginConfig> getConfigs(StoreMerchantId merchantStore) {
		return service.getConfigs(merchantStore);
	}

	@PostMapping
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUA.*')")
	public void saveConfigs(StoreMerchantId merchantStore, @RequestBody List<SocialLoginConfig> configs) {
		service.saveConfigs(merchantStore, configs);
	}

	@GetMapping("/supported-social-providers")
	public SocialProvider[] getSupportedProviders() {
		return SocialProvider.values();
	}

}
