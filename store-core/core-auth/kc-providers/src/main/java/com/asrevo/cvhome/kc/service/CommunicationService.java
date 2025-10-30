package com.asrevo.cvhome.kc.service;

import com.asrevo.cvhome.kc.email.provider.TokenInterceptor;

public interface CommunicationService {

	void sendMessage(String address, String htmlBody);

	void setTokenInterceptor(TokenInterceptor tokenInterceptor);

}
