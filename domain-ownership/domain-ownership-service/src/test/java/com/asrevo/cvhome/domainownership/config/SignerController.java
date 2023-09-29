package com.asrevo.cvhome.domainownership.config;

import com.nimbusds.jose.JOSEException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SignerController {
    private final JwtSigner jwtSigner;


    public SignerController(JwtSigner jwtSigner) {
        this.jwtSigner = jwtSigner;
    }

    @PostMapping("api/v1/test/sign")
    public Map<String, String> sign(@RequestBody Map<String, Object> claims) throws JOSEException {
        return Map.of("access_token", jwtSigner.createJwt(claims));
    }
}


