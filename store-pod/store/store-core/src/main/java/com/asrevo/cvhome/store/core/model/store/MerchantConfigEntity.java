package com.asrevo.cvhome.store.core.model.store;

import com.asrevo.cvhome.store.core.entity.system.MerchantConfigurationType;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantConfigEntity extends Entity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String key;
    private MerchantConfigurationType type;
    private String value;
    private boolean active;

}
