package com.asrevo.cvhome.tenancy.manager.service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.commons.dto.OrgStatus;
import com.asrevo.cvhome.tenancy.errors.IllegalLifecycleTransitionException;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerOrgMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The organization's own profile and status.
 *
 * <p>
 * Suspending an organization closes every store it owns, but does <em>not</em> write a status onto each of them.
 * The org owns its status and {@code InternalStoreService.requireOperable} reads both — a fan-out write would
 * drift the moment one update failed, and would then disagree with itself about who is suspended.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrgLifecycleService {

    private static final String STATUS = "STATUS";

    private static final Map<OrgStatus, Set<OrgStatus>> ALLOWED = Map.of(
            OrgStatus.ACTIVE, EnumSet.of(OrgStatus.SUSPENDED, OrgStatus.CLOSED),
            OrgStatus.SUSPENDED, EnumSet.of(OrgStatus.ACTIVE, OrgStatus.CLOSED),
            OrgStatus.CLOSED, EnumSet.noneOf(OrgStatus.class));

    private final ManagerOrgRepository orgRepository;

    private final ManagerOrgMappers orgMappers;

    private final TenancyAuditService auditService;

    /** Renames an organization and records who did it. */
    @Transactional
    public ManagerOrgDto rename(ManagerOrgId org, String name, String actor) throws OrgNotFoundException {
        ManagerOrgEntity entity = entity(org);
        String previous = entity.getName();
        entity.setName(name);
        ManagerOrgDto saved = orgMappers.toDto(orgRepository.save(entity));
        auditService.record(AuditEntityType.ORG, org, "RENAME", previous, name, actor, null);
        return saved;
    }

    @Transactional
    public ManagerOrgDto suspend(ManagerOrgId org, String actor, String reason)
            throws OrgNotFoundException, IllegalLifecycleTransitionException {
        return move(org, OrgStatus.SUSPENDED, actor, reason);
    }

    @Transactional
    public ManagerOrgDto resume(ManagerOrgId org, String actor)
            throws OrgNotFoundException, IllegalLifecycleTransitionException {
        return move(org, OrgStatus.ACTIVE, actor, "resumed by operator");
    }

    @Transactional
    public ManagerOrgDto close(ManagerOrgId org, String actor)
            throws OrgNotFoundException, IllegalLifecycleTransitionException {
        return move(org, OrgStatus.CLOSED, actor, "closed by operator");
    }

    private ManagerOrgDto move(ManagerOrgId org, OrgStatus to, String actor, String detail)
            throws OrgNotFoundException, IllegalLifecycleTransitionException {
        ManagerOrgEntity entity = entity(org);
        OrgStatus from = Objects.requireNonNullElse(entity.getStatus(), OrgStatus.ACTIVE);
        if (from == to) {
            auditService.record(AuditEntityType.ORG, org, STATUS, from, to, actor,
                    String.format("no-op: %s", detail));
            return orgMappers.toDto(entity);
        }
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw IllegalLifecycleTransitionException.of(org, from, to);
        }
        entity.setStatus(to);
        ManagerOrgDto saved = orgMappers.toDto(orgRepository.save(entity));
        auditService.record(AuditEntityType.ORG, org, STATUS, from, to, actor, detail);
        log.info("Org {} moved {} -> {} by {}; every store it owns follows", org, from, to, actor);
        return saved;
    }

    private ManagerOrgEntity entity(ManagerOrgId org) throws OrgNotFoundException {
        return orgRepository.findById(org).orElseThrow(() -> OrgNotFoundException.of(org));
    }

}
