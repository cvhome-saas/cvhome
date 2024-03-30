package com.asrevo.cvhome.store.core.entity.system;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "MODULE_CONFIGURATION", indexes = {
        @Index(name = "MODULE_CONFIGURATION_MODULE", columnList = "MODULE")})
@Getter
@Setter

public class IntegrationModule extends SalesManagerEntity<Long, IntegrationModule> implements Serializable, Auditable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "MODULE_CONF_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "MOD_CONF_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "MODULE")
    private String module;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "REGIONS")
    private String regions;

    @Column(name = "CONFIGURATION", length = 4000)
    private String configuration;

    @Column(name = "DETAILS", columnDefinition = "text")
    private String configDetails;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "IMAGE")
    private String image;

    @Column(name = "CUSTOM_IND")
    private boolean customModule = false;

    @Transient
    private Set<String> regionsSet = new HashSet<String>();

    @Transient
    private String binaryImage = null;

    /**
     * Contains a map of module config by environment (DEV,PROD)
     */
    @Transient
    private Map<String, ModuleConfig> moduleConfigs = new HashMap<String, ModuleConfig>();

    @Transient
    private Map<String, String> details = new HashMap<String, String>();

    /**
     * A json tructure decribing how the module must be built
     */
    @Transient
    private String configurable = null;


    @Embedded
    private AuditSection auditSection = new AuditSection();

}
