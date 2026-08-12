package com.asrevo.cvhome.tenancy.commons.dto;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;

/**
 * @param name        the organization's display name, null for organizations created before it existed
 * @param status      whether it may be used; suspending one closes every store it owns
 * @param ownerUserId uaa's id for whoever owns it
 */
public record ManagerOrgDto(ManagerOrgId id, Email email, Instant createdDate, String name, OrgStatus status,
                            String ownerUserId) {
}
