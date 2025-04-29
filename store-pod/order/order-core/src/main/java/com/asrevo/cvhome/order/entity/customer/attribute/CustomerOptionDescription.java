package com.asrevo.cvhome.order.entity.customer.attribute;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "CUSTOMER_OPTION_DESC",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"CUSTOMER_OPTION_ID", "LANGUAGE_CODE"})
        })
@TableGenerator(
        name = "description_gen",
        table = "SM_SEQUENCER",
        pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT",
        pkColumnValue = "CUSTOMER_OPTION_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class CustomerOptionDescription extends Description {
    @Serial private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = CustomerOption.class)
    @JoinColumn(name = "CUSTOMER_OPTION_ID", nullable = false)
    private CustomerOption customerOption;

    @Column(name = "CUSTOMER_OPTION_COMMENT", length = 4000)
    private String customerOptionComment;

    public CustomerOptionDescription() {}
}
