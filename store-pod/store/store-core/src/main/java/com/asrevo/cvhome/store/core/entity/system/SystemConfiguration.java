package com.asrevo.cvhome.store.core.entity.system;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Global system configuration information
 *
 * @author casams1
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SYSTEM_CONFIGURATION")
@Getter
@Setter
public class SystemConfiguration extends SalesManagerEntity<Long, SystemConfiguration> implements Serializable, Auditable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SYSTEM_CONFIG_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SYST_CONF_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "CONFIG_KEY")
    private String key;

    @Column(name = "VALUE")
    private String value;

    @Embedded
    private AuditSection auditSection = new AuditSection();
}
