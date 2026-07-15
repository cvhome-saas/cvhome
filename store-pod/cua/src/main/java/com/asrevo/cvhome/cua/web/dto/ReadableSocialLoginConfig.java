package com.asrevo.cvhome.cua.web.dto;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.config.SocialProvider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadableSocialLoginConfig {
    private StoreMerchantId storeMerchantId;
    private SocialProvider providerId;
    private String appId;
    private String appSecret;
    private Boolean enabled;
}
