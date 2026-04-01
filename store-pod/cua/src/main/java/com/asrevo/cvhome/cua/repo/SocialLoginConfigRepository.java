package com.asrevo.cvhome.cua.repo;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.config.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialLoginConfigRepository extends JpaRepository<SocialLoginConfig, SocialLoginConfigId> {

	List<SocialLoginConfig> findAllById_StoreMerchantId(StoreMerchantId storeMerchantId);

	default List<SocialLoginConfigId> findSocialLoginConfigByClientId(StoreMerchantId storeMerchantId) {
		return findAllById_StoreMerchantId(storeMerchantId).stream().map(SocialLoginConfig::getId).toList();
	}

}
