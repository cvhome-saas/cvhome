package com.asrevo.cvhome.cua.web;

import com.asrevo.cvhome.cua.dto.ReadableUser;
import com.asrevo.cvhome.cua.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final UserService userService;

	@GetMapping("me")
	public ResponseEntity<Object> me() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof JwtAuthenticationToken auth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		UUID id = UUID.fromString((String) auth.getTokenAttributes().get("sub"));
		ReadableUser user = userService.getById(id).orElseThrow();
		return ResponseEntity.ok(user);
	}

}
