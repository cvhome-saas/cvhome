package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

public class StoreMerchantIdSerializeParamArgumentResolver implements HttpServiceArgumentResolver {

	@Override
	public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
		try {
			if (argument instanceof StoreMerchantId store) {
				requestValues.addRequestParameter("store", store.getId());
				return true;
			}
		}
		catch (Exception ignored) {
		}
		return false;
	}

}
