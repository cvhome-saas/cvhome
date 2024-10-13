package com.asrevo.cvhome.store.core.model.store;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantStoreBrand {

    private List<MerchantConfigEntity> socialNetworks = new ArrayList<>();
}
