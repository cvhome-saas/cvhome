package com.asrevo.cvhome.uaa.web.pub;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.dto.AcceptLinkRequest;
import com.asrevo.cvhome.uaa.dto.AcceptedLink;
import com.asrevo.cvhome.uaa.dto.LinkPreview;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.ResetTokenNotUsableException;
import com.asrevo.cvhome.uaa.invitation.PasswordResetService;

import lombok.RequiredArgsConstructor;

/** Where a password-reset link lands. Same contract as the invitation endpoints: the token is the credential. */
@RestController
@RequestMapping("/api/v1/public/password-resets")
@RequiredArgsConstructor
public class PublicPasswordResetController {

    private final PasswordResetService resets;

    @GetMapping("{token}")
    public LinkPreview preview(@PathVariable String token) throws ResetTokenNotUsableException {
        return resets.preview(token);
    }

    @PostMapping("{token}/accept")
    public AcceptedLink accept(@PathVariable String token, @Valid @RequestBody AcceptLinkRequest req)
            throws ResetTokenNotUsableException, PasswordPolicyViolationException, PasswordReusedException,
            PasswordCompromisedException {
        return resets.accept(token, req.password());
    }

}
