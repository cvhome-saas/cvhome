package com.asrevo.cvhome.store.core.entity.customer.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "CUSTOMER_OPTION_VALUE", indexes = {@Index(name = "CUST_OPT_VAL_CODE_IDX", columnList = "CUSTOMER_OPT_VAL_CODE")}, uniqueConstraints =
@UniqueConstraint(columnNames = {"MERCHANT_ID", "CUSTOMER_OPT_VAL_CODE"}))
@Getter
@Setter
public class CustomerOptionValue extends SalesManagerEntity<Long, CustomerOptionValue> {
    private static final long serialVersionUID = 3736085877929910891L;

    @Id
    @Column(name = "CUSTOMER_OPTION_VALUE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "CUSTOMER_OPT_VAL_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Column(name = "CUSTOMER_OPT_VAL_IMAGE")
    private String customerOptionValueImage;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "CUSTOMER_OPT_VAL_CODE")
    private String code;


    @Valid
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "customerOptionValue")
    private Set<CustomerOptionValueDescription> descriptions = new HashSet<CustomerOptionValueDescription>();

    @Transient
    private List<CustomerOptionValueDescription> descriptionsList = new ArrayList<CustomerOptionValueDescription>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    public CustomerOptionValue() {
    }

    public List<CustomerOptionValueDescription> getDescriptionsSettoList() {
        if (descriptionsList == null || descriptionsList.size() == 0) {
            descriptionsList = new ArrayList<CustomerOptionValueDescription>(this.getDescriptions());
        }
        return descriptionsList;
    }




}
