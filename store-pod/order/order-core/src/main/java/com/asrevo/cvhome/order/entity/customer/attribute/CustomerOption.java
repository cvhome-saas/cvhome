package com.asrevo.cvhome.order.entity.customer.attribute;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "CUSTOMER_OPTION",
        indexes = {@Index(name = "CUST_OPT_CODE_IDX", columnList = "CUSTOMER_OPT_CODE")},
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CUSTOMER_OPT_CODE"}))
@Getter
@Setter
public class CustomerOption extends SalesManagerEntity<Long, CustomerOption> {
    @Serial private static final long serialVersionUID = -2019269055342226086L;

    @Id
    @Column(name = "CUSTOMER_OPTION_ID")
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "CUSTOMER_OPTION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Column(name = "CUSTOMER_OPTION_TYPE", length = 10)
    private String customerOptionType;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "CUSTOMER_OPT_CODE")
    // @Index(name="CUST_OPT_CODE_IDX")
    private String code;

    @Column(name = "CUSTOMER_OPT_ACTIVE")
    private boolean active;

    @Column(name = "CUSTOMER_OPT_PUBLIC")
    private boolean publicOption;

    @Valid
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "customerOption")
    private Set<CustomerOptionDescription> descriptions = new HashSet<>();

    @Transient private List<CustomerOptionDescription> descriptionsList = new ArrayList<>();

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(
                    name = "storeMerchantId",
                    column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    public CustomerOption() {}

    public List<CustomerOptionDescription> getDescriptionsSettoList() {
        if (descriptionsList == null || descriptionsList.isEmpty()) {
            descriptionsList = new ArrayList<>(this.getDescriptions());
        }
        return descriptionsList;
    }
}
