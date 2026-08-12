package com.asrevo.cvhome.tenancy.commons.dto;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

/**
 * Someone who belongs to an organization.
 *
 * @param userId uaa's id for the user — a string, because uaa issues UUIDs rather than the ObjectIds the
 *               {@code commons/domain} value objects wrap
 */
public record OrgMemberDto(ManagerOrgId orgId, String userId, String role, Instant addedAt, String addedBy) {
}
