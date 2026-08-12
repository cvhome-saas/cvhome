package com.asrevo.cvhome.tenancy.manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.OrgMemberDto;
import com.asrevo.cvhome.tenancy.manager.entity.OrgMemberEntity;
import com.asrevo.cvhome.tenancy.manager.repository.OrgMemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Who belongs to an organization. Membership is granted by accepting an invitation, or directly by an admin. */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrgMemberService {

    private final OrgMemberRepository memberRepository;

    private final TenancyAuditService auditService;

    public List<OrgMemberDto> list(ManagerOrgId org) {
        return memberRepository.findByOrg(org.getId().toString()).stream().map(OrgMemberService::toDto).toList();
    }

    /** Tolerates a user who already belongs, so a repeated grant is not an error. */
    @Transactional
    public boolean add(ManagerOrgId org, String userId, String role, String actor) {
        int added = memberRepository.add(org.getId().toString(), userId, role, actor);
        if (added == 0) {
            return false;
        }
        auditService.record(AuditEntityType.MEMBER, org, "ADD", null, role, actor,
                String.format("added %s", userId));
        return true;
    }

    @Transactional
    public boolean remove(ManagerOrgId org, String userId, String actor) {
        int removed = memberRepository.remove(org.getId().toString(), userId);
        if (removed == 0) {
            return false;
        }
        auditService.record(AuditEntityType.MEMBER, org, "REMOVE", null, null, actor,
                String.format("removed %s", userId));
        log.info("Removed {} from org {} by {}", userId, org, actor);
        return true;
    }

    private static OrgMemberDto toDto(OrgMemberEntity entity) {
        return new OrgMemberDto(entity.getOrgId(), entity.getUserId(), entity.getRole(), entity.getAddedAt(),
                entity.getAddedBy());
    }

}
