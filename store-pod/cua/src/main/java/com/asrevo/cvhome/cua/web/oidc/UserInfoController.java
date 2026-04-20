package com.asrevo.cvhome.cua.web.oidc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInfoController {

    private static final String CLAIMS_EMAIL_KEY = "email";

    private static final String CLAIMS_NAME_KEY = "name";

    private static final String CLAIMS_ROLES_KEY = "roles";

    private static final String CLAIMS_SUB_KEY = "sub";

    @GetMapping("/userinfo")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return Map.of();
        }
        Map<String, Object> out = new HashMap<>();
        out.put(CLAIMS_SUB_KEY, jwt.getSubject());
        out.put(CLAIMS_EMAIL_KEY, jwt.getClaimAsString(CLAIMS_EMAIL_KEY));
        out.put(CLAIMS_NAME_KEY, jwt.getClaimAsString(CLAIMS_NAME_KEY));
        List<String> roles = jwt.getClaimAsStringList(CLAIMS_ROLES_KEY);
        if (roles != null) {
            out.put(CLAIMS_ROLES_KEY, roles);
        }
        return out;
    }

}
