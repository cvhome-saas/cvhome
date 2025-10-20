package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;

public record StoreMerchantId(String storeMerchantId) implements Comparable<StoreMerchantId>, Serializable {

	public String getId() {
		return this.storeMerchantId;
	}

	@Override
	public int compareTo(StoreMerchantId o) {
		return this.storeMerchantId.compareTo(o.storeMerchantId);
	}
}
