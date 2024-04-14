package com.asrevo.cvhome.store.core.entity.customer.attribute;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Table(name = "CUSTOMER_OPTION_SET",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "CUSTOMER_OPTION_ID",
                        "CUSTOMER_OPTION_VALUE_ID"
                })
        }
)
@Getter
@Setter
public class CustomerOptionSet extends SalesManagerEntity<Long, CustomerOptionSet> {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_OPTIONSET_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "CUST_OPTSET_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_OPTION_ID", nullable = false)
    private CustomerOption customerOption = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_OPTION_VALUE_ID", nullable = false)
    private CustomerOptionValue customerOptionValue = null;


    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

}
