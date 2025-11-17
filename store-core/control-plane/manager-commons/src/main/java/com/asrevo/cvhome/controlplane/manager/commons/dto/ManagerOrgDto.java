package com.asrevo.cvhome.controlplane.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import java.time.Instant;

public record ManagerOrgDto(ManagerOrgId id, Email email, Instant createdDate) {
}
