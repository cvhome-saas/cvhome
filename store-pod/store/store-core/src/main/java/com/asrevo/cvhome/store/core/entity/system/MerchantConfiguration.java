package com.asrevo.cvhome.store.core.entity.system;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private static final long serialVersionUID = 4246917986731953459L;

    @Id
    @Column(name = "MERCHANT_CONFIG_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "MERCH_CONF_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = true)
    private MerchantStore merchantStore;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "CONFIG_KEY")
    private String key;

    /**
     * activate and deactivate configuration
     */
    @Column(name = "ACTIVE", nullable = true)
    private Boolean active = Boolean.FALSE;


    @Column(name = "VALUE", columnDefinition = "text")
    private String value;

    @Column(name = "TYPE")
    @Enumerated(value = EnumType.STRING)
    private MerchantConfigurationType merchantConfigurationType =
            MerchantConfigurationType.INTEGRATION;

}
