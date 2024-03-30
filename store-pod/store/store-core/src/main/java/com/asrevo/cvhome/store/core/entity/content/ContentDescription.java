package com.asrevo.cvhome.store.core.entity.content;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "CONTENT_DESCRIPTION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "CONTENT_ID",
                "LANGUAGE_ID"
        })
}
)

@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "content_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
//@SequenceGenerator(name = "description_gen", sequenceName = "content_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_SEQUENCE_START)
@Getter
@Setter
public class ContentDescription extends Description implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = Content.class)
    @JoinColumn(name = "CONTENT_ID", nullable = false)
    private Content content;

    @Column(name = "SEF_URL", length = 120)
    private String seUrl;


    @Column(name = "META_KEYWORDS")
    private String metatagKeywords;

    @Column(name = "META_TITLE")
    private String metatagTitle;
    @Column(name = "META_DESCRIPTION")
    private String metatagDescription;

    public ContentDescription() {
    }

    public ContentDescription(String name, Language language) {
        this.setName(name);
        this.setLanguage(language);
        super.setId(0L);
    }


}
