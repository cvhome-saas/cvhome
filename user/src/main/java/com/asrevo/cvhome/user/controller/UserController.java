package com.asrevo.cvhome.user.controller;

import com.asrevo.cvhome.user.domain.User;
import com.asrevo.cvhome.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/user")
@Slf4j
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping(params = "userId")
    public Mono<User> findOne(@RequestParam(value = "userId") Long id) {
        return userRepository.findById(id);
    }
}
