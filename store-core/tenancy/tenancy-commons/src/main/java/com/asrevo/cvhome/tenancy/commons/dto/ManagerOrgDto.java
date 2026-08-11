package com.asrevo.cvhome.tenancy.commons.dto;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;

public record ManagerOrgDto(ManagerOrgId id, Email email, Instant createdDate) {
}
