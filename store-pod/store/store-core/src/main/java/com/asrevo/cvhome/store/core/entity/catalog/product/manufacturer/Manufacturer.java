package com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "MANUFACTURER", uniqueConstraints =
@UniqueConstraint(columnNames = {"MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class Manufacturer extends SalesManagerEntity<Long, Manufacturer> implements Auditable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "MANUFACTURER_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "MANUFACT_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ManufacturerDescription> descriptions = new HashSet<ManufacturerDescription>();

    @Column(name = "MANUFACTURER_IMAGE")
    private String image;

    @Column(name = "SORT_ORDER")
    private Integer order = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @NotEmpty
    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    public Manufacturer() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public AuditSection getAuditSection() {
        return auditSection;
    }

    @Override
    public void setAuditSection(AuditSection auditSection) {
        this.auditSection = auditSection;
    }



}
