package com.asrevo.cvhome.sso.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.sso.domain.ClientExtension;

public interface ClientExtensionRepository extends JpaRepository<ClientExtension, String> {

    List<ClientExtension> findByEnabledFalse();

}
