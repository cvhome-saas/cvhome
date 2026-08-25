package com.asrevo.cvhome.tenancy.manager.entity;

import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.ReflectionUtils;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.events.store.StoreCreatedEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The store aggregate's own transitions.
 *
 * <p>
 * The failure reason is taken as an argument to {@code failProvisioning} rather than left to a setter, because a
 * FAILED row without one is the state that change exists to remove — and it is truncated here rather than at the
 * caller, because {@code manager_store.provisioning_error} is {@code varchar(500)} and a pod's problem detail can
 * run longer than that.
 * </p>
 */
class ManagerStoreEntityTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final int PROVISIONING_ERROR_COLUMN = 500;

    private static final String STORE_NAME = "a-store";

    private static final String POD_REFUSAL = "the pod refused it";

    private static final String MISSING_FIELD = "email must not be null";

    private static ManagerStoreEntity created() {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setName(STORE_NAME);
        return ManagerStoreEntity.createStore(request, ORG, POD);
    }

    @Test
    void aCreatedStoreStartsActiveAndUnprovisioned() {
        ManagerStoreEntity entity = created();

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getName()).isEqualTo(STORE_NAME);
        assertThat(entity.getOrgId()).isEqualTo(ORG);
        assertThat(entity.getPodId()).isEqualTo(POD);
        assertThat(entity.getStatus()).isEqualTo(StoreStatus.ACTIVE);
        assertThat(entity.getProvisioningState()).isEqualTo(ProvisioningState.NOT_STARTED_PROVISIONING);
        assertThat(entity.getCreatedDate()).isNotNull();
    }

    /**
     * Creation registers the event rather than calling the pod: the outbox is what makes provisioning, billing and
     * the registry's capacity count retry independently of each other and of this transaction.
     *
     * <p>
     * {@code domainEvents()} is protected on Spring Data's aggregate root, so the registration is read the way
     * Spring Data itself reads it — reflectively.
     * </p>
     */
    @Test
    void creationRegistersTheEventThatDrivesProvisioning() {
        ManagerStoreEntity entity = created();

        assertThat(registeredEvents(entity)).singleElement()
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(StoreCreatedEvent.class))
                .satisfies(event -> {
                    assertThat(event.store()).isEqualTo(entity.getId());
                    assertThat(event.orgId()).isEqualTo(ORG);
                    assertThat(event.podId()).isEqualTo(POD);
                });
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> registeredEvents(ManagerStoreEntity entity) {
        Method accessor = ReflectionUtils.findMethod(AbstractAggregateRoot.class, "domainEvents");
        ReflectionUtils.makeAccessible(accessor);
        return (Collection<Object>) ReflectionUtils.invokeMethod(accessor, entity);
    }

    @Test
    void completingProvisioningClearsTheReasonOfAnEarlierFailure() {
        ManagerStoreEntity entity = created().failProvisioning(POD_REFUSAL);

        assertThat(entity.completeProvisioning().getProvisioningState())
                .isEqualTo(ProvisioningState.SUCCESSFULLY_PROVISIONING);
        assertThat(entity.getProvisioningError()).isNull();
    }

    /** A retry that is about to run again must not still be showing the previous attempt's reason. */
    @Test
    void startingProvisioningClearsTheReasonOfAnEarlierFailure() {
        ManagerStoreEntity entity = created().failProvisioning(POD_REFUSAL);

        assertThat(entity.startProvisioning().getProvisioningState())
                .isEqualTo(ProvisioningState.IN_PROGRESS_PROVISIONING);
        assertThat(entity.getProvisioningError()).isNull();
    }

    @Test
    void aFailureRecordsItsReasonAlongsideTheState() {
        ManagerStoreEntity entity = created().failProvisioning(MISSING_FIELD);

        assertThat(entity.getProvisioningState()).isEqualTo(ProvisioningState.FAILED_PROVISIONING);
        assertThat(entity.getProvisioningError()).isEqualTo(MISSING_FIELD);
    }

    @Test
    void aReasonLongerThanTheColumnIsTruncatedRatherThanFailingTheInsert() {
        ManagerStoreEntity entity = created().failProvisioning("x".repeat(PROVISIONING_ERROR_COLUMN + 200));

        assertThat(entity.getProvisioningError()).hasSize(PROVISIONING_ERROR_COLUMN);
    }

    @Test
    void aBlankReasonIsRecordedAsNoReasonAtAll() {
        assertThat(created().failProvisioning("   ").getProvisioningError()).isNull();
        assertThat(created().failProvisioning(null).getProvisioningError()).isNull();
    }

}
