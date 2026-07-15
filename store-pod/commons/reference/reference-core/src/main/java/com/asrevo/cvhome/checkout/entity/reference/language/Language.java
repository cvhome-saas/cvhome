package com.asrevo.cvhome.checkout.entity.reference.language;

import java.io.Serial;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "LANGUAGE", indexes = {@Index(name = "CODE_IDX2", columnList = "CODE")})
@Cacheable
@Getter
@Setter
public class Language extends SalesManagerEntity<LanguageCode, Language> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonSerialize(using = LanguageCodeSerializer.class)
    @JsonDeserialize(using = LanguageCodeDeSerializer.class)
    @AttributeOverride(name = "code", column = @Column(name = "CODE", length = 6))
    private LanguageCode code;

    @JsonIgnore
    @Embedded
    private AuditSection auditSection = new AuditSection();

    @JsonIgnore
    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    public Language() {
    }

    public Language(LanguageCode code) {
        this.setCode(code);
    }

    @Override
    public LanguageCode getId() {
        return code;
    }

    @Override
    public void setId(LanguageCode id) {
        this.code = id;
    }
}
