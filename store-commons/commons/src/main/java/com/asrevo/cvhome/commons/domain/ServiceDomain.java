package com.asrevo.cvhome.commons.domain;

public record ServiceDomain(String name, String domain, String port, String schema, String namespace,
		String gatewayServiceName) {
	public String getServiceHost() {
		return schema() + "://" + domain() + ":" + port();
	}

	public String getServiceHost(String service) {
		return schema() + "://" + domain() + ":" + port() + "/" + service + "/";
	}
}
