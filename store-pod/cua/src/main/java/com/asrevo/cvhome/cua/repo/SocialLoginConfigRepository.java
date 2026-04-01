package com.asrevo.cvhome.cua.repo;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialLoginConfigRepository extends JpaRepository<SocialLoginConfig, SocialLoginConfigId> {

	List<SocialLoginConfig> findAllById_StoreMerchantId(StoreMerchantId storeMerchantId);

	default List<SocialLoginConfigId> findEnabledSocialLoginConfig(StoreMerchantId storeMerchantId) {
		return findAllById_StoreMerchantId(storeMerchantId).stream()
			.filter(SocialLoginConfig::getEnabled)
			.map(SocialLoginConfig::getId)
			.toList();
	}

}
