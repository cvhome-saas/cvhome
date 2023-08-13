package com.asrevo.cvhome.user.repository;

import com.asrevo.cvhome.user.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
