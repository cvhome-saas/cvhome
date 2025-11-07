package com.asrevo.cvhome.commons.domain;

public record Pod(PodId id, String name, PodEndpoint endpoint, ManagerOrgId orgId, String domain) {
}
