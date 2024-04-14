package com.asrevo.cvhome.store.core.entity.content;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CONTENT",
        indexes = {@Index(name = "CODE_IDX", columnList = "CODE")},
        uniqueConstraints = @UniqueConstraint(columnNames = {"MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class Content extends SalesManagerEntity<Long, Content> implements Serializable {


    @Serial
    private static final long serialVersionUID = 1772757159185494620L;

    @Id
    @Column(name = "CONTENT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Valid
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentDescription> descriptions = new ArrayList<ContentDescription>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @NotEmpty
    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    @Column(name = "VISIBLE")
    private boolean visible;

    @Column(name = "LINK_TO_MENU")
    private boolean linkToMenu;

    @Column(name = "CONTENT_POSITION", length = 10, nullable = true)
    @Enumerated(value = EnumType.STRING)
    private ContentPosition contentPosition;

    //Used for grouping
    //BOX, SECTION, PAGE
    @Column(name = "CONTENT_TYPE", length = 10, nullable = true)
    @Enumerated(value = EnumType.STRING)
    private ContentType contentType;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    //A page can contain one product listing
    @Column(name = "PRODUCT_GROUP", nullable = true)
    private String productGroup;

    public ContentDescription getDescription() {

        if (this.getDescriptions() != null && !this.getDescriptions().isEmpty()) {
            return this.getDescriptions().get(0);
        }

        return null;

    }
}