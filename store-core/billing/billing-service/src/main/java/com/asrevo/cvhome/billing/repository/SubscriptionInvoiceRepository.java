package com.asrevo.cvhome.billing.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

public interface SubscriptionInvoiceRepository extends CrudRepository<SubscriptionInvoiceEntity, StripeInvoiceId>,
        PagingAndSortingRepository<SubscriptionInvoiceEntity, StripeInvoiceId> {

    /**
     * A store's invoices, newest first, scoped to the org that owns it.
     *
     * <p>
     * The org is part of the query rather than checked afterwards, for the same reason as the subscription read: the
     * shared permission checker cannot tell which org a store belongs to, so the boundary has to be in the SQL.
     * </p>
     */
    Page<SubscriptionInvoiceEntity> findAllByStoreIdAndOrgIdOrderByIssuedAtDesc(ManagerStoreId storeId,
                                                                                ManagerOrgId orgId,
                                                                                Pageable pageable);

    Page<SubscriptionInvoiceEntity> findAllByStoreIdOrderByIssuedAtDesc(ManagerStoreId storeId, Pageable pageable);

    List<SubscriptionInvoiceEntity> findAllByStoreId(ManagerStoreId storeId);

}
