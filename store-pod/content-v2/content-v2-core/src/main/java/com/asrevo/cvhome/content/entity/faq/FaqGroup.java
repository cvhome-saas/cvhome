package com.asrevo.cvhome.content.entity.faq;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FAQ_GROUP")
@Getter
@Setter
public class FaqGroup {
    @Id
    @Column(name = "GROUP_ID")
    @TableGenerator(name = "faq_group_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "FAQ_GROUP_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "faq_group_gen")
    private Long id;
    @Embedded
    @AttributeOverride(name = "storeMerchantId", column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId storeMerchantId;
    @Column(name = "CODE", nullable = false, length = 100)
    private String code;
    @Column(name = "POSITION", nullable = false)
    private int position;
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FaqGroupDescription> descriptions = new HashSet<>();

    public void addDescription(FaqGroupDescription description) {
        description.setGroup(this);
        descriptions.add(description);
    }
}
