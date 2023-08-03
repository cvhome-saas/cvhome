package com.asrevo.cvhome.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequestMapping("api/v1/user")
public class UserApplication {
    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @GetMapping(params = "userId")
    public Mono<User> findOne(@RequestParam(value = "userId") String id) {
        return userRepository.findById(id);
    }
}

@Table("x_user")
record User(@Id String id, String name, String email) {
}

interface UserRepository extends ReactiveCrudRepository<User, String> {

}