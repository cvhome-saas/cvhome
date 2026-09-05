package com.asrevo.cvhome.checkout.entity.order.orderproduct;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_PRODUCT_DOWNLOAD")
@Getter
@Setter
public class OrderProductDownload extends SalesManagerEntity<Long, OrderProductDownload> implements Serializable {

    public static final int DEFAULT_DOWNLOAD_MAX_DAYS = 31;

    @Serial
    private static final long serialVersionUID = -8935511990745477240L;

    @Id
    @Column(name = "ORDER_PRODUCT_DOWNLOAD_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_PRODUCT_DOWNLOAD_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ORDER_PRODUCT_ID", nullable = false)
    private OrderProduct orderProduct;

    @Column(name = "ORDER_PRODUCT_FILENAME", nullable = false)
    private String orderProductFilename;

    @Column(name = "DOWNLOAD_MAXDAYS", nullable = false)
    private Integer maxdays = DEFAULT_DOWNLOAD_MAX_DAYS;

    @Column(name = "DOWNLOAD_COUNT", nullable = false)
    private Integer downloadCount;

}
