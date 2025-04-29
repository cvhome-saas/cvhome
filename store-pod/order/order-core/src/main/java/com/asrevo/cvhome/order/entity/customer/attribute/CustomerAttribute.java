package com.asrevo.cvhome.order.entity.customer.attribute;

import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "CUSTOMER_ATTRIBUTE",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"OPTION_ID", "CUSTOMER_ID"})})
@Getter
@Setter
public class CustomerAttribute extends SalesManagerEntity<Long, CustomerAttribute> {
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_ATTRIBUTE_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "CUSTOMER_ATTRIBUTE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_ID", nullable = false)
    private CustomerOption customerOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_VALUE_ID", nullable = false)
    private CustomerOptionValue customerOptionValue;

    @Column(name = "CUSTOMER_ATTR_TXT_VAL")
    private String textValue;

    @JsonIgnore
    @ManyToOne(targetEntity = Customer.class)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    public CustomerAttribute() {}
}
