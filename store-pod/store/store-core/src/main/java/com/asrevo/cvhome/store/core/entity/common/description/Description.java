package com.asrevo.cvhome.store.core.entity.common.description;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@MappedSuperclass
@EntityListeners(value = AuditListener.class)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public class Description implements Auditable, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DESCRIPTION_ID")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "description_gen")
    private Long id;

    @JsonIgnore
    @Embedded
    private AuditSection auditSection = new AuditSection();

    @ManyToOne(optional = false)
    @JoinColumn(name = "LANGUAGE_ID")
    private Language language;

    @NotEmpty
    @Column(name = "NAME", nullable = false, length = 120)
    private String name;

    @Column(name = "TITLE", length = 100)
    private String title;

    @Column(name = "DESCRIPTION", columnDefinition = "text")
    private String description;

    public Description() {
    }

    public Description(Language language, String name) {
        this.setLanguage(language);
        this.setName(name);
    }
}
