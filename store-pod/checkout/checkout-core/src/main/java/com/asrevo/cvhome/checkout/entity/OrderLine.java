package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * What was bought, as it was at placement: the catalog can rename or delete the product afterwards and this line
 * keeps saying the same thing.
 */
@Entity
@Table(name = "SALES_ORDER_LINE")
@Getter
@Setter
public class OrderLine extends SalesManagerEntity<Long, OrderLine> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "LINE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "SALES_ORDER_LINE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Column(name = "SKU", nullable = false)
    private String sku;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name = "UNIT_PRICE", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @Column(name = "LINE_TOTAL", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Column(name = "IMAGE_URL", length = 1024)
    private String imageUrl;

    @Column(name = "SORT_ORDER", nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<OrderLineOption> options = new ArrayList<>();

    public OrderLine() {
    }

    public OrderLine(Order order, String sku, Long productId, String productName, BigDecimal unitPrice,
                     int quantity, String imageUrl, int sortOrder) {
        this.order = order;
        this.sku = sku;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public void addOption(String optionName, String valueName) {
        options.add(new OrderLineOption(this, optionName, valueName, options.size()));
    }
}
