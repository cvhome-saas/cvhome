package com.asrevo.cvhome.uaa.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.uaa.domain.ClientExtension;

public interface ClientExtensionRepository extends JpaRepository<ClientExtension, String> {

    List<ClientExtension> findByEnabledFalse();

}
