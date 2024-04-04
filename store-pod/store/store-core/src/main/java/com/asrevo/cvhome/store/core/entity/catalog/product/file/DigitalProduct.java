package com.asrevo.cvhome.store.core.entity.catalog.product.file;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;


/**
 * Representation of a digital product
 *
 * @author csamson777
 */
@Entity
@Table(name = "PRODUCT_DIGITAL", uniqueConstraints =
@UniqueConstraint(columnNames = {"PRODUCT_ID", "FILE_NAME"}))
@Getter
@Setter
public class DigitalProduct extends SalesManagerEntity<Long, DigitalProduct> {


    @Serial
    private static final long serialVersionUID = 1L;


    @Id
    @Column(name = "PRODUCT_DIGITAL_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_DGT_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;


    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;


    @Column(name = "FILE_NAME", nullable = false)
    private String productFileName;


}
