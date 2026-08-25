package com.asrevo.cvhome.tenancy.manager.dto;

import com.asrevo.cvhome.uaa.domain.user.PersistableUser;

public record CreateOrgRequest(PersistableUser user) {
}
