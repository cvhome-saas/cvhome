package com.asrevo.cvhome.store.core.model.store;

import com.asrevo.cvhome.store.core.model.references.MeasureUnit;
import com.asrevo.cvhome.store.core.model.references.WeightUnit;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class MerchantStoreEntity implements Serializable {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    @NotNull
    private String code;
    @NotNull
    private String name;
    private String org;

    private String defaultLanguage;//code
    private String currency;//code
    private String inBusinessSince;
    @NotNull
    private String email;
    @NotNull
    private String phone;
    private String template;

    private boolean useCache;
    private boolean currencyFormatNational;
    private boolean retailer;
    private MeasureUnit dimension;
    private WeightUnit weight;


}
