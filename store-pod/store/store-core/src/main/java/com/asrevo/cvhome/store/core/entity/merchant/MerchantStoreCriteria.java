package com.asrevo.cvhome.store.core.entity.merchant;

import com.asrevo.cvhome.store.core.entity.common.Criteria;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantStoreCriteria extends Criteria {

    private boolean retailers = false;
    private boolean stores = false;


}
