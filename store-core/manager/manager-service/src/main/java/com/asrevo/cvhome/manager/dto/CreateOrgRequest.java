package com.asrevo.cvhome.manager.dto;

import com.asrevo.cvhome.keycloak.domain.user.PersistableUser;

public record CreateOrgRequest(PersistableUser user) {}
