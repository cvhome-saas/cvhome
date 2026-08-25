package com.asrevo.cvhome.merchant.model.merchant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.commons.domain.ColorTheme;
import com.asrevo.cvhome.commons.domain.Theme;
import com.asrevo.cvhome.store.core.model.MerchantStorePricingBase;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.asrevo.cvhome.store.model.references.WeightUnit;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantStoreDetails extends MerchantStorePricingBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private String id;

    @NotNull
    private String name;

    private String org;

    private Theme theme;

    private ColorTheme colorTheme;

    private LocalDate inBusinessSince;

    @NotNull
    private String email;

    @NotNull
    private String phone;

    private String template;

    private boolean useCache;

    private boolean requireLoginForOrderPlacement;

    private MeasureUnit dimension;

    private WeightUnit weight;

}
