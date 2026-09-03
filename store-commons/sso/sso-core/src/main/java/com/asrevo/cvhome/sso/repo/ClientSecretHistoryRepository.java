package com.asrevo.cvhome.sso.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.sso.domain.ClientSecretHistory;

public interface ClientSecretHistoryRepository extends JpaRepository<ClientSecretHistory, UUID> {

    List<ClientSecretHistory> findByRegisteredClientIdAndRevokedAtIsNull(String registeredClientId);

}
