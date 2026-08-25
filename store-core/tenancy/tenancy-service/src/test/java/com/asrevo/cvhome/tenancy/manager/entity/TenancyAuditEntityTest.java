package com.asrevo.cvhome.tenancy.manager.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.AuditSource;
import com.asrevo.cvhome.tenancy.commons.dto.OrgStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How an audited id reaches its column.
 *
 * <p>
 * This guards a specific bug. {@code entity_id} is {@code varchar(24)}, and the callers hand this factory a value
 * object; {@code String.valueOf(someStoreId)} yields {@code StoreMerchantId[id=65f0…]} — forty-odd characters — so
 * every audited change failed on the insert and, being inside the same transaction as the change, took the change
 * with it. Unwrapping lives here so no caller has to remember it.
 * </p>
 */
class TenancyAuditEntityTest {

    private static final String STORE_HEX = "65f023632bc46470c104b76f";

    private static final String ORG_HEX = "21f023932bc66470c104b76f";

    private static final int ENTITY_ID_COLUMN = 24;

    private static final String ACTOR = "operator";

    private static final String STATUS = "STATUS";

    private static final String BEFORE = "Before";

    private static final String AFTER = "After";

    private static final String RENAMED = "renamed";

    private static final String INVITATION_ID = "an-invitation-id";

    @Test
    void aValueObjectIsUnwrappedToItsBareIdRatherThanItsToString() {
        TenancyAuditEntity entity = TenancyAuditEntity.of(AuditEntityType.STORE, new StoreMerchantId(STORE_HEX),
                STATUS, null, null, ACTOR, AuditSource.API, null);

        assertThat(entity.getEntityId()).isEqualTo(STORE_HEX).hasSizeLessThanOrEqualTo(ENTITY_ID_COLUMN);
    }

    @Test
    void anOrganizationIdIsUnwrappedTheSameWay() {
        TenancyAuditEntity entity = TenancyAuditEntity.of(AuditEntityType.ORG, new ManagerOrgId(ORG_HEX), "RENAME",
                BEFORE, AFTER, ACTOR, AuditSource.API, RENAMED);

        assertThat(entity.getEntityId()).isEqualTo(ORG_HEX);
        assertThat(entity.getFromState()).isEqualTo(BEFORE);
        assertThat(entity.getToState()).isEqualTo(AFTER);
        assertThat(entity.getDetail()).isEqualTo(RENAMED);
        assertThat(entity.getSource()).isEqualTo(AuditSource.API);
        assertThat(entity.getRecordedAt()).isNotNull();
    }

    /** An invitation id is already a bare string, and must survive unchanged. */
    @Test
    void aPlainStringIdIsLeftAlone() {
        TenancyAuditEntity entity = TenancyAuditEntity.of(AuditEntityType.INVITATION, INVITATION_ID, "REVOKE",
                null, null, ACTOR, AuditSource.API, null);

        assertThat(entity.getEntityId()).isEqualTo(INVITATION_ID);
    }

    @Test
    void aNullEntityStaysNullRatherThanBecomingTheStringNull() {
        TenancyAuditEntity entity = TenancyAuditEntity.of(AuditEntityType.MEMBER, null, "ADD", null, null, ACTOR,
                AuditSource.JOB, null);

        assertThat(entity.getEntityId()).isNull();
        assertThat(entity.getFromState()).isNull();
        assertThat(entity.getToState()).isNull();
    }

    /** Enum states are rendered by name, which is what the {@code varchar(30)} state columns hold. */
    @Test
    void anEnumStateIsRecordedByName() {
        TenancyAuditEntity entity = TenancyAuditEntity.of(AuditEntityType.ORG, new ManagerOrgId(ORG_HEX), STATUS,
                OrgStatus.ACTIVE, OrgStatus.SUSPENDED, ACTOR, AuditSource.API, "unpaid");

        assertThat(entity.getFromState()).isEqualTo("ACTIVE");
        assertThat(entity.getToState()).isEqualTo("SUSPENDED");
    }

}
