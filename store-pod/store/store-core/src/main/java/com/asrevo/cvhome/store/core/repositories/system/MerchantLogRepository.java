package com.asrevo.cvhome.store.core.repositories.system;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.MerchantLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantLogRepository extends JpaRepository<MerchantLog, Long> {

    List<MerchantLog> findByStore(MerchantStore store);
}
