package com.asrevo.cvhome.store.core.entity.catalog.marketplace;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.Embedded;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

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
    @Serial private static final long serialVersionUID = 1L;

    private Long id;

    private MerchantStore store;

    private String code;

    private List<CatalogDescription> descriptions = new ArrayList<>();

    @Embedded private AuditSection auditSection = new AuditSection();
}
