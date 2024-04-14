package com.asrevo.cvhome.store.core.entity.reference.language;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "LANGUAGE", indexes = {@Index(name = "CODE_IDX2", columnList = "CODE")})
@Cacheable
@Getter
@Setter
public class Language extends SalesManagerEntity<Integer, Language> implements Auditable {
    @Serial
    private static final long serialVersionUID = 1L;


    @Id
    @Column(name = "LANGUAGE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "LANG_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Integer id;

    @JsonIgnore
    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "CODE", nullable = false)
    private String code;

    @JsonIgnore
    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    @JsonIgnore
    @OneToMany(mappedBy = "defaultLanguage", targetEntity = MerchantStore.class)
    private List<MerchantStore> storesDefaultLanguage;

    @JsonIgnore
    @ManyToMany(mappedBy = "languages", targetEntity = MerchantStore.class, fetch = FetchType.LAZY)
    private List<MerchantStore> stores = new ArrayList<MerchantStore>();

    public Language() {
    }

    public Language(String code) {
        this.setCode(code);
    }


    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (!(obj instanceof Language)) {
            return false;
        } else {
            Language language = (Language) obj;
            return (this.id == language.getId());
        }
    }
}
