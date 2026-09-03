package com.asrevo.cvhome.uaa.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.uaa.domain.IdentityProvider;

public interface IdentityProviderRepository extends JpaRepository<IdentityProvider, UUID> {

    Optional<IdentityProvider> findByAlias(String alias);

    boolean existsByAlias(String alias);

    List<IdentityProvider> findAllByOrderBySortOrderAscDisplayNameAsc();

    List<IdentityProvider> findByEnabledTrueOrderBySortOrderAscDisplayNameAsc();

}
