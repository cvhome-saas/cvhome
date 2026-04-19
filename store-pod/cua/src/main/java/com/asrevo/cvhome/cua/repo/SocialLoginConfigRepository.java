package com.asrevo.cvhome.cua.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;

@Repository
public interface SocialLoginConfigRepository extends JpaRepository<SocialLoginConfig, SocialLoginConfigId> {

    @Query("SELECT s FROM SocialLoginConfig s WHERE s.id.storeMerchantId = :storeMerchantId")
    List<SocialLoginConfig> findAllByIdStoreMerchantId(StoreMerchantId storeMerchantId);

    default List<SocialLoginConfigId> findEnabledSocialLoginConfig(StoreMerchantId storeMerchantId) {
        return findAllByIdStoreMerchantId(storeMerchantId).stream()
                .filter(SocialLoginConfig::getEnabled)
                .map(SocialLoginConfig::getId)
                .toList();
    }

}
