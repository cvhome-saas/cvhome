package com.asrevo.cvhome.store.core.model.store;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class MerchantStoreBrand {


    private List<MerchantConfigEntity> socialNetworks = new ArrayList<MerchantConfigEntity>();

}
