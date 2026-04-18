package com.asrevo.cvhome.store.core.modules.cms.model;

import java.io.Closeable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CmsProductImage implements Closeable {

    private Long id;

    private StoreMerchantId storeMerchantId;

    private String sku;

    private String productImage;

    @Override
    public void close() {
    }

}
