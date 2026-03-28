package com.asrevo.cvhome.cua.repo;

import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialLoginConfigRepository extends JpaRepository<SocialLoginConfig, UUID> {

	List<SocialLoginConfig> findAllByClientId(String clientId);

	Optional<SocialLoginConfig> findByClientIdAndProviderId(String clientId, String providerId);

}
