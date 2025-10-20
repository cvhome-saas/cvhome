package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUser;
import com.asrevo.cvhome.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.manager.service.SignupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/user-account")
@AllArgsConstructor
@Slf4j
public class SignUpController {

	private final SignupService signupService;

	@PostMapping("public/create")
	@ConditionalOnApiStatus
	public Mono<ReadableUser> create(@RequestBody CreateOrgRequest request) {
		return Mono.just(signupService.createOrgUser(request));
	}

}
