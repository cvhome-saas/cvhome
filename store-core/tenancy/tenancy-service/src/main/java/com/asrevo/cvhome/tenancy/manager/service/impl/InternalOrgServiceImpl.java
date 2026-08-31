package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.ListOrgQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerOrgMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;
import com.asrevo.cvhome.tenancy.manager.service.InternalOrgService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class InternalOrgServiceImpl implements InternalOrgService {

    private static final int DEFAULT_PAGE_SIZE = 100;

    private final ManagerOrgRepository managerOrgRepository;

    private final ManagerOrgMappers managerOrgMappers;

    @Transactional
    @Override
    public ManagerOrgId createOrgForUser(Email email, String name) {
        ManagerOrgEntity entity = managerOrgRepository.save(ManagerOrgEntity.createOrgFromUser(email, name));
        return entity.getId();
    }

    @Override
    public Page<ManagerOrgDto> findAll(Pageable pageable) {
        return findAll(null, pageable);
    }

    /**
     * One page of organizations, narrowed by the query.
     *
     * <p>
     * The count is a separate query because Spring Data JDBC's {@code @Query} has no {@code countQuery} attribute
     * — that is JPA's — so the page is assembled here, the same way {@code InternalStoreServiceImpl} does it.
     * </p>
     *
     * <p>
     * A blank term is normalised to null rather than searched for: {@code ilike '%%'} matches everything, which is
     * the right answer by accident, and relying on that would make an empty box and a cleared box different code
     * paths for no reason.
     * </p>
     */
    @Override
    public Page<ManagerOrgDto> findAll(ListOrgQuery query, Pageable pageable) {
        Pageable page = pageable == null || pageable.isUnpaged() ? Pageable.ofSize(DEFAULT_PAGE_SIZE) : pageable;
        String term = query == null || !StringUtils.hasText(query.term()) ? null : query.term().trim();
        String status = query == null || query.status() == null ? null : query.status().name();
        List<ManagerOrgEntity> rows =
                managerOrgRepository.findVisible(term, status, page.getPageSize(), page.getOffset());
        long total = managerOrgRepository.countVisible(term, status);
        return new PageImpl<>(rows.stream().map(managerOrgMappers::toDto).toList(), page, total);
    }

    /**
     * One organization, or a typed 404.
     *
     * <p>
     * This ended in a bare {@code orElseThrow()} — a {@code NoSuchElementException}, which the error handler can
     * only read as a 500. An unknown org id is the caller's mistake, not the server's, and every other method on
     * this aggregate ({@code rename}, {@code suspend}, {@code resume}, {@code close}) already said so with
     * {@link OrgNotFoundException}.
     * </p>
     */
    @Override
    public ManagerOrgDto findOne(ManagerOrgId id) throws OrgNotFoundException {
        return managerOrgMappers.toDto(entity(id));
    }

    /**
     * Records the owning uaa account.
     *
     * <p>
     * {@code manager_org.owner_user_id} has existed as a column and as a DTO field since the lifecycle work and
     * <strong>nothing ever wrote it</strong>: {@code ManagerOrgEntity.createOrgFromUser} sets id, created date,
     * email and status, and no other path assigned an owner. The column was null for every organization on the
     * platform, which is what made {@code OrgManagerApi.changePassword} unimplementable.
     * </p>
     */
    @Transactional
    @Override
    public void recordOwner(ManagerOrgId id, String ownerUserId) throws OrgNotFoundException {
        ManagerOrgEntity entity = entity(id);
        entity.setOwnerUserId(ownerUserId);
        managerOrgRepository.save(entity);
        log.info("Org {} owner recorded as {}", id, ownerUserId);
    }

    private ManagerOrgEntity entity(ManagerOrgId id) throws OrgNotFoundException {
        return managerOrgRepository.findById(id).orElseThrow(() -> OrgNotFoundException.of(id));
    }

}
