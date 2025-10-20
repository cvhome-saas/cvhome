package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.manager.utils.Defines;
import com.asrevo.cvhome.s2s.model.AppProperties;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/saas")
@AllArgsConstructor
@Slf4j
public class SaasController {

	private final AppProperties appProperties;

	@RequestMapping("public/saas-properties")
	public Map<String, String> saasProperties() {
		return Map.of("alis", Defines.SAAS_POD_SUFFIX, "domain", appProperties.getDomain());
	}

}
