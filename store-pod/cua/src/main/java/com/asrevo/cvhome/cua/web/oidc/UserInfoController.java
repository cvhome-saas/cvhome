package com.asrevo.cvhome.cua.web.oidc;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserInfoController {

	@GetMapping("/userinfo")
	public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
		if (jwt == null)
			return Map.of();
		Map<String, Object> out = new HashMap<>();
		out.put("sub", jwt.getSubject());
		out.put("email", jwt.getClaimAsString("email"));
		out.put("name", jwt.getClaimAsString("name"));
		List<String> roles = jwt.getClaimAsStringList("roles");
		if (roles != null)
			out.put("roles", roles);
		return out;
	}

}
