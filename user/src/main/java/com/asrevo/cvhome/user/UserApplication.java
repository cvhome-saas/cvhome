package com.asrevo.cvhome.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
    public User findOne(@RequestParam(value = "userId") String id) {
        return userRepository.findOne(id);
    }
}

record User(String id, String name, String email) {
}

interface UserRepository {
    User findOne(String id);
}

@Service
class UserRepositoryImpl implements UserRepository {
    private final Map<String, User> dbUserMap = Map.of("1", new User("1", "jhon", "jhon@email.com"));

    @Override
    public User findOne(String id) {
        return dbUserMap.get(id);
    }
}
