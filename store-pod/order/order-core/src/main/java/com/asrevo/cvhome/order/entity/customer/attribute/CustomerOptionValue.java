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
        name = "CUSTOMER_OPTION_VALUE",
        indexes = {@Index(name = "CUST_OPT_VAL_CODE_IDX", columnList = "CUSTOMER_OPT_VAL_CODE")},
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CUSTOMER_OPT_VAL_CODE"}))
@Getter
@Setter
public class CustomerOptionValue extends SalesManagerEntity<Long, CustomerOptionValue> {
    @Serial private static final long serialVersionUID = 3736085877929910891L;

    @Id
    @Column(name = "CUSTOMER_OPTION_VALUE_ID")
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "CUSTOMER_OPTION_VALUE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Column(name = "CUSTOMER_OPT_VAL_IMAGE")
    private String customerOptionValueImage;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "CUSTOMER_OPT_VAL_CODE")
    private String code;

    @Valid
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "customerOptionValue")
    private Set<CustomerOptionValueDescription> descriptions = new HashSet<>();

    @Transient private List<CustomerOptionValueDescription> descriptionsList = new ArrayList<>();

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(
                    name = "storeMerchantId",
                    column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    public CustomerOptionValue() {}

    public List<CustomerOptionValueDescription> getDescriptionsSettoList() {
        if (descriptionsList == null || descriptionsList.isEmpty()) {
            descriptionsList = new ArrayList<>(this.getDescriptions());
        }
        return descriptionsList;
    }
}
