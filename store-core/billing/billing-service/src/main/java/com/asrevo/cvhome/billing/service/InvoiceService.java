package com.asrevo.cvhome.billing.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.commons.dto.InvoiceView;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

/**
 * Billing history.
 */
public interface InvoiceService {

    /**
     * A store's invoices, newest first.
     *
     * @param scopeOrg the caller's org; the listing cannot reach a store outside it. {@code null} only for a caller
     *                 entitled to span orgs.
     */
    Page<InvoiceView> list(ManagerStoreId store, ManagerOrgId scopeOrg, Pageable pageable);

}
