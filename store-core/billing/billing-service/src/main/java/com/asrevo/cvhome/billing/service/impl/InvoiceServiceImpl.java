package com.asrevo.cvhome.billing.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.dto.InvoiceView;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;
import com.asrevo.cvhome.billing.mappers.InvoiceMappers;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.service.InvoiceService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final SubscriptionInvoiceRepository invoiceRepository;

    private final InvoiceMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceView> list(StoreMerchantId store, ManagerOrgId scopeOrg, Pageable pageable) {
        Page<SubscriptionInvoiceEntity> page = scopeOrg == null
                ? invoiceRepository.findAllByStoreIdOrderByIssuedAtDesc(store, pageable)
                : invoiceRepository.findAllByStoreIdAndOrgIdOrderByIssuedAtDesc(store, scopeOrg, pageable);
        return page.map(mappers::toView);
    }

}
