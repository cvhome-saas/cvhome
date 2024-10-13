package com.asrevo.cvhome.store.core.model.store;

import com.asrevo.cvhome.store.core.model.content.ReadableImage;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableBrand extends MerchantStoreBrand {

    private ReadableImage logo;
    private ReadableImage banner;
}
