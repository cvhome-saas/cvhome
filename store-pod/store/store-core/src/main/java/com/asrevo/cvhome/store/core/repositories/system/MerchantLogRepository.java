package com.asrevo.cvhome.store.core.repositories.system;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.MerchantLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MerchantLogRepository extends JpaRepository<MerchantLog, Long> {

    List<MerchantLog> findByStore(MerchantStore store);
}
