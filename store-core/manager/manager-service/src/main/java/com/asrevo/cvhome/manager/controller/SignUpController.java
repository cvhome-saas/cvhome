package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.keycloak.domain.user.PersistableUser;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUser;
import com.asrevo.cvhome.keycloak.service.UserAccountService;
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
    private final UserAccountService userAccountService;

    @PostMapping("public/create")
    public Mono<ReadableUser> create(@RequestBody PersistableUser user) {
        user.setActive(true);
        user.setUserName(user.getEmailAddress());
        return Mono.just(userAccountService.createOrgUser(user));

    }

}
