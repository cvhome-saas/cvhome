package com.asrevo.cvhome.store.core.entity.system;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Merchant configuration information
 *
 * @author Carl Samson
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "MERCHANT_CONFIGURATION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MERCHANT_ID", "CONFIG_KEY"}))
@Getter
@Setter
public class MerchantConfiguration extends SalesManagerEntity<Long, MerchantConfiguration>
        implements Serializable, Auditable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 4246917986731953459L;

    @Id
    @Column(name = "MERCHANT_CONFIG_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "MERCH_CONF_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID")
    private MerchantStore merchantStore;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "CONFIG_KEY")
    private String key;

    /**
     * activate and deactivate configuration
     */
    @Column(name = "ACTIVE")
    private Boolean active = Boolean.FALSE;


    @Column(name = "VALUE", columnDefinition = "text")
    private String value;

    @Column(name = "TYPE")
    @Enumerated(value = EnumType.STRING)
    private MerchantConfigurationType merchantConfigurationType =
            MerchantConfigurationType.INTEGRATION;

}
