package com.asrevo.cvhome.store.core.entity.tax.taxclass;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.tax.taxrate.TaxRate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TAX_CLASS",
        indexes = {@Index(name = "TAX_CLASS_CODE_IDX", columnList = "TAX_CLASS_CODE")},
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"MERCHANT_ID", "TAX_CLASS_CODE"}))
@Getter
@Setter


public class TaxClass extends SalesManagerEntity<Long, TaxClass> {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "TAX_CLASS_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "TX_CLASS_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;
    @NotEmpty
    @Column(name = "TAX_CLASS_CODE", nullable = false, length = 10)
    private String code;
    @NotEmpty
    @Column(name = "TAX_CLASS_TITLE", nullable = false, length = 32)
    private String title;
    @OneToMany(mappedBy = "taxClass", targetEntity = Product.class)
    private List<Product> products = new ArrayList<Product>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = true)
    private MerchantStore merchantStore;
    @OneToMany(mappedBy = "taxClass")
    private List<TaxRate> taxRates = new ArrayList<TaxRate>();


    public TaxClass(String code) {
        this.code = code;
        this.title = code;
    }

    public TaxClass() {
        super();
    }

}
