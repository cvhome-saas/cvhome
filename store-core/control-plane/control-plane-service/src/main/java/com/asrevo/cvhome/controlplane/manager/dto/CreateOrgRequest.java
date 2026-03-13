package com.asrevo.cvhome.controlplane.manager.dto;

import com.asrevo.cvhome.uaa.domain.user.PersistableUser;

public record CreateOrgRequest(PersistableUser user) {
}
