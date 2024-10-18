package com.asrevo.cvhome.store.core.repositories.system;

import com.asrevo.cvhome.store.core.entity.system.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {

    SystemConfiguration findByKey(String key);
}
