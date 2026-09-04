package com.asrevo.cvhome.sso.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.sso.domain.UserIdentity;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    Optional<UserIdentity> findByProviderIdAndSubject(UUID providerId, String subject);

    List<UserIdentity> findByUserIdOrderByLinkedAtAsc(UUID userId);

    long countByUserId(UUID userId);

    long countByProviderId(UUID providerId);

}
