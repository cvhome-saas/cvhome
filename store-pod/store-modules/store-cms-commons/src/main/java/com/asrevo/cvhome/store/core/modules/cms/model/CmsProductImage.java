package com.asrevo.cvhome.store.core.modules.cms.model;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.io.Closeable;
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
