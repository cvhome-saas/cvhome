package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One option/value label of a variant line ("Color" / "Red"), snapshotted at placement.
 */
@Entity
@Table(name = "SALES_ORDER_LINE_OPTION")
@Getter
@Setter
public class OrderLineOption extends SalesManagerEntity<Long, OrderLineOption> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "OPTION_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "SALES_ORDER_LINE_OPTION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LINE_ID", nullable = false)
    private OrderLine line;

    @Column(name = "OPTION_NAME", nullable = false, length = 120)
    private String optionName;

    @Column(name = "VALUE_NAME", nullable = false, length = 120)
    private String valueName;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    public OrderLineOption() {
    }

    public OrderLineOption(OrderLine line, String optionName, String valueName, int sortOrder) {
        this.line = line;
        this.optionName = optionName;
        this.valueName = valueName;
        this.sortOrder = sortOrder;
    }
}
