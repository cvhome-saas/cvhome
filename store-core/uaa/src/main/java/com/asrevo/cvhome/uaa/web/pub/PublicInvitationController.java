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
import com.asrevo.cvhome.uaa.errors.InvitationNotUsableException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.invitation.InvitationService;

import lombok.RequiredArgsConstructor;

/**
 * Where an invitation link lands. Public, stateless, rate limited; the token in the path is the whole credential.
 * Every refusal is the same 404.
 */
@RestController
@RequestMapping("/api/v1/public/invitations")
@RequiredArgsConstructor
public class PublicInvitationController {

    private final InvitationService invitations;

    @GetMapping("{token}")
    public LinkPreview preview(@PathVariable String token) throws InvitationNotUsableException {
        return invitations.preview(token);
    }

    @PostMapping("{token}/accept")
    public AcceptedLink accept(@PathVariable String token, @Valid @RequestBody AcceptLinkRequest req)
            throws InvitationNotUsableException, PasswordPolicyViolationException, PasswordReusedException,
            PasswordCompromisedException {
        return invitations.accept(token, req.password());
    }

}
