package com.asrevo.cvhome.controlplane.manager.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.controlplane.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.controlplane.manager.service.SignupService;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/user-account")
@AllArgsConstructor
@Slf4j
public class SignUpController {

    private final SignupService signupService;

    @PostMapping("public/create")

    public ReadableUser create(@RequestBody CreateOrgRequest request)
            throws UaaConflictException, UaaApiUnavailableException {
        return signupService.createOrgUser(request);
    }

}
