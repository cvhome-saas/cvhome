package com.asrevo.cvhome.store.core.entity.customer.attribute;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Table(name = "CUSTOMER_OPT_VAL_DESCRIPTION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "CUSTOMER_OPT_VAL_ID",
                "LANGUAGE_ID"
        })
}
)

@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "customer_option_value_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
//@SequenceGenerator(name = "description_gen", sequenceName = "customer_option_value_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_SEQUENCE_START)
@Getter
@Setter
public class CustomerOptionValueDescription extends Description {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @ManyToOne(targetEntity = CustomerOptionValue.class)
    @JoinColumn(name = "CUSTOMER_OPT_VAL_ID")
    private CustomerOptionValue customerOptionValue;

}
