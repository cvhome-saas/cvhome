package com.asrevo.cvhome.merchant.model.merchant;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.store.core.model.MerchantStorePricingBase;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.asrevo.cvhome.store.model.references.WeightUnit;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantStoreEntity extends MerchantStorePricingBase implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    @NotNull private String id;
    @NotNull private String name;
    private String org;
    private Theme theme;
    private ColorTheme colorTheme;
    private Set<SocialLink> socialLinks;

    private String inBusinessSince;
    @NotNull private String email;
    @NotNull private String phone;
    private String template;

    private boolean useCache;
    private MeasureUnit dimension;
    private WeightUnit weight;
}
