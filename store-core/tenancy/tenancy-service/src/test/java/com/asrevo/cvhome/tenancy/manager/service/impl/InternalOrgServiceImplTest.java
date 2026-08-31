package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.ListOrgQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.commons.dto.OrgStatus;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerOrgMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Listing organizations, and recording who owns one.
 *
 * <p>
 * A blank search term is normalised to no filter rather than searched for. {@code ilike '%%'} matches everything,
 * which is the right answer by accident, and relying on that would make an empty box and a cleared box two
 * different code paths for no reason.
 * </p>
 */
class InternalOrgServiceImplTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final int PAGE_SIZE = 25;

    private static final int DEFAULT_PAGE_SIZE = 100;

    private static final String OWNER_ID = "an-owner-id";

    private static final String FOUNDER_EMAIL = "founder@example.com";

    private static final String ORG_NAME = "Nordwerk";

    /** Whitespace with nothing in it, which several rules here have to treat as absence rather than as a value. */
    private static final String BLANK = "   ";

    private ManagerOrgRepository repository;

    private InternalOrgServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ManagerOrgRepository.class);
        ManagerOrgMappers mappers = mock(ManagerOrgMappers.class);
        when(mappers.toDto(any())).thenAnswer(it -> {
            ManagerOrgEntity entity = it.getArgument(0);
            return new ManagerOrgDto(entity.getId(), null, null, entity.getName(), entity.getStatus(),
                    entity.getOwnerUserId());
        });
        when(repository.findVisible(any(), any(), anyInt(), anyLong())).thenReturn(List.of());
        service = new InternalOrgServiceImpl(repository, mappers);
    }

    /** The term and status the query was actually narrowed by. */
    private String[] filtersOf(ListOrgQuery query) {
        service.findAll(query, PageRequest.of(0, PAGE_SIZE));
        ArgumentCaptor<String> term = ArgumentCaptor.captor();
        ArgumentCaptor<String> status = ArgumentCaptor.captor();
        verify(repository).findVisible(term.capture(), status.capture(), anyInt(), anyLong());
        return new String[] {term.getValue(), status.getValue()};
    }

    @Test
    void aBlankTermIsNormalisedToNoFilterAtAll() {
        assertThat(filtersOf(new ListOrgQuery(BLANK, null))).containsExactly(null, null);
    }

    @Test
    void aTermIsTrimmedBeforeItReachesTheQuery() {
        assertThat(filtersOf(new ListOrgQuery("  acme  ", null))).containsExactly("acme", null);
    }

    @Test
    void aStatusIsPassedByName() {
        assertThat(filtersOf(new ListOrgQuery(null, OrgStatus.CLOSED))).containsExactly(null, "CLOSED");
    }

    @Test
    void noQueryAtAllNarrowsNothing() {
        assertThat(filtersOf(null)).containsExactly(null, null);
    }

    @Test
    void theUnfilteredListingIsTheSameQueryWithNoTerm() {
        service.findAll(Pageable.unpaged());

        verify(repository).findVisible(eq(null), eq(null), eq(DEFAULT_PAGE_SIZE), eq(0L));
    }

    @Test
    void thePageCarriesTheMatchingTotalRatherThanTheSliceSize() {
        when(repository.countVisible(any(), any())).thenReturn(97L);

        assertThat(service.findAll(null, PageRequest.of(0, PAGE_SIZE)).getTotalElements()).isEqualTo(97L);
    }

    /**
     * This ended in a bare {@code orElseThrow()} — a {@code NoSuchElementException}, which the error handler can
     * only read as a 500. An unknown org id is the caller's mistake, not the server's.
     */
    @Test
    void anUnknownOrganizationIsATypedNotFoundRatherThanAServerFault() {
        when(repository.findById(ORG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOne(ORG)).isInstanceOf(OrgNotFoundException.class);
        assertThatThrownBy(() -> service.recordOwner(ORG, "someone")).isInstanceOf(OrgNotFoundException.class);
    }

    /**
     * {@code manager_org.owner_user_id} shipped with the lifecycle work and had no writer, so it was null for every
     * organization on the platform — which is what made {@code OrgManagerApi.changePassword} unimplementable.
     */
    @Test
    void recordingAnOwnerWritesItBack() throws Exception {
        ManagerOrgEntity entity = new ManagerOrgEntity();
        when(repository.findById(ORG)).thenReturn(Optional.of(entity));

        service.recordOwner(ORG, OWNER_ID);

        assertThat(entity.getOwnerUserId()).isEqualTo(OWNER_ID);
        verify(repository).save(entity);
    }

    @Test
    void anOrganizationIsCreatedFromTheSignupEmail() {
        ManagerOrgEntity saved = ManagerOrgEntity.createOrgFromUser(new Email(FOUNDER_EMAIL), ORG_NAME);
        when(repository.save(any())).thenReturn(saved);

        assertThat(service.createOrgForUser(new Email(FOUNDER_EMAIL), ORG_NAME)).isEqualTo(saved.getId());
        assertThat(saved.getName()).isEqualTo(ORG_NAME);
    }

    @Test
    void anOrganizationWithNoNameKeepsANullColumnRatherThanAnEmptyOne() {
        // The column is nullable and the console's list screen falls back to the contact email when it is null.
        // An empty string would defeat that fallback and show a blank cell instead.
        assertThat(ManagerOrgEntity.createOrgFromUser(new Email(FOUNDER_EMAIL), BLANK).getName()).isNull();
    }

}
