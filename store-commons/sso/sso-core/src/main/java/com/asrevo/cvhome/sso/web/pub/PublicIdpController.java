package com.asrevo.cvhome.sso.web.pub;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.sso.dto.PublicIdpDto;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;

import lombok.RequiredArgsConstructor;

/**
 * What the sign-in page needs about providers before anyone is signed in: the buttons, and which provider an email
 * belongs to. A hidden provider is not a button but still answers discovery — hiding is about the button.
 */
@RestController
@RequestMapping("/api/v1/public/idps")
@RequiredArgsConstructor
public class PublicIdpController {

    private final IdentityProviderService providers;

    @GetMapping
    public List<PublicIdpDto> visible() {
        return providers.visibleForLogin();
    }

    /** Answers {@code {"alias": …}} or {@code {"alias": null}}; never which accounts exist. */
    @PostMapping("discover")
    public Map<String, Object> discover(@Valid @RequestBody DiscoverRequest req) {
        Map<String, Object> answer = new java.util.HashMap<>();
        answer.put("provider", providers.discoverByEmail(req.email()).orElse(null));
        return answer;
    }

    public record DiscoverRequest(@NotBlank String email) {
    }

}
