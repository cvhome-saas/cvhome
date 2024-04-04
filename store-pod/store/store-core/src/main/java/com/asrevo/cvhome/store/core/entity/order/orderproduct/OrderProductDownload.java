package com.asrevo.cvhome.store.core.entity.order.orderproduct;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "ORDER_PRODUCT_DOWNLOAD")
@Getter
@Setter
public class OrderProductDownload extends SalesManagerEntity<Long, OrderProductDownload> implements Serializable {
    public final static int DEFAULT_DOWNLOAD_MAX_DAYS = 31;
    @Serial
    private static final long serialVersionUID = -8935511990745477240L;
    @Id
    @Column(name = "ORDER_PRODUCT_DOWNLOAD_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_PRODUCT_DL_ID_NEXT_VALUE")
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