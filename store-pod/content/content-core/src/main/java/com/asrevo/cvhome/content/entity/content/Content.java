package com.asrevo.cvhome.content.entity.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.content.ContentPosition;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(
        name = "CONTENT",
        indexes = {@Index(name = "CODE_IDX", columnList = "CODE")},
        uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class Content extends SalesManagerEntity<Long, Content> implements Serializable {

    @Serial private static final long serialVersionUID = 1772757159185494620L;

    @Id
    @Column(name = "CONTENT_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "CONTENT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded private AuditSection auditSection = new AuditSection();

    @Valid
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentDescription> descriptions = new ArrayList<>();

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(
                    name = "storeMerchantId",
                    column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    @NotEmpty
    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    @Column(name = "VISIBLE")
    private boolean visible;

    @Column(name = "LINK_TO_MENU")
    private boolean linkToMenu;

    @Column(name = "CONTENT_POSITION", length = 10)
    @Enumerated(value = EnumType.STRING)
    private ContentPosition contentPosition;

    // Used for grouping
    // BOX, SECTION, PAGE
    @Column(name = "CONTENT_TYPE", length = 10)
    @Enumerated(value = EnumType.STRING)
    private ContentType contentType;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    // A page can contain one product listing
    @Column(name = "PRODUCT_GROUP")
    private String productGroup;

    public ContentDescription getDescription() {

        if (this.getDescriptions() != null && !this.getDescriptions().isEmpty()) {
            return this.getDescriptions().getFirst();
        }

        return null;
    }
}
