package com.asrevo.cvhome.cua.domain;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.config.SocialProvider;
import com.asrevo.cvhome.store.core.converter.ZoneCodeConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "social_login_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLoginConfig implements Serializable {

	@EmbeddedId
	private SocialLoginConfigId id;

	@Column(name = "app_id", nullable = false)
	private String appId;

	@Column(name = "app_secret", nullable = false)
	private String appSecret;

	@Column(name = "enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
	private Boolean enabled = true;

}
