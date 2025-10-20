package com.asrevo.cvhome.kc.email.provider;

public interface TokenInterceptor {

	default String getAccessToken() {
		return null;
	}

}
