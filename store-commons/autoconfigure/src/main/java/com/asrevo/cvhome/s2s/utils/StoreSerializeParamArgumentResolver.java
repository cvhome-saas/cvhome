package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

public class StoreSerializeParamArgumentResolver implements HttpServiceArgumentResolver {

	@Override
	public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
		try {
			if (argument instanceof ManagerStoreId store) {
				String name = "store";
				String value = store.getId().toString();
				requestValues.addRequestParameter(name, value);
				return true;
			}
		}
		catch (Exception ignored) {
		}
		return false;
	}

}
