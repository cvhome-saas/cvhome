package com.asrevo.cvhome.subscription.commons;

import com.asrevo.cvhome.commons.domain.Identifier;

public record PriceId(String id) implements Identifier {
	@Override
	public Object getId() {
		return id;
	}
}
