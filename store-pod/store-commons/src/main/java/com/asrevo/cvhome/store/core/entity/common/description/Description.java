package com.asrevo.cvhome.store.core.entity.common.description;

import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@EntityListeners(value = AuditListener.class)
@Getter
@Setter
public class Description implements Auditable, Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DESCRIPTION_ID")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "description_gen")
    private Long id;

    @JsonIgnore @Embedded private AuditSection auditSection = new AuditSection();

    @Column(name = "LANGUAGE_CODE", length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    @JsonSerialize(using = LanguageCodeSerializer.class)
    @JsonDeserialize(using = LanguageCodeDeSerializer.class)
    private LanguageCode languageCode;

    @NotEmpty
    @Column(name = "NAME", nullable = false, length = 120)
    private String name;

    @Column(name = "TITLE", length = 100)
    private String title;

    @Column(name = "DESCRIPTION", columnDefinition = "text")
    private String description;

    public Description() {}
}
