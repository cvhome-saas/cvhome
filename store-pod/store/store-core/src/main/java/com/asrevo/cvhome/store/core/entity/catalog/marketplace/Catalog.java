package com.asrevo.cvhome.store.core.entity.catalog.marketplace;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A catalog is used to classify products of a given merchant
 * to be displayed in a specific marketplace
 *
 * @author c.samson
 */
@Getter
@Setter
public class Catalog extends SalesManagerEntity<Long, Catalog> implements Auditable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Long id;

    private MerchantStore store;

    private String code;

    private List<CatalogDescription> descriptions = new ArrayList<CatalogDescription>();

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Override
    public AuditSection getAuditSection() {
        return auditSection;
    }

    @Override
    public void setAuditSection(AuditSection audit) {
        this.auditSection = audit;
    }


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

}
