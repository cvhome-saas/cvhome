package com.asrevo.cvhome.store.core.entity.reference.currency;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "CURRENCY")
@Cacheable
@Getter
@Setter
public class Currency extends SalesManagerEntity<Long, Currency> implements Serializable {
    @Serial
    private static final long serialVersionUID = -999926410367685145L;

    @Id
    @Column(name = "CURRENCY_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "CURRENCY_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "CURRENCY_CURRENCY_CODE", nullable = false, unique = true)
    private java.util.Currency currency;

    @Column(name = "CURRENCY_SUPPORTED")
    private Boolean supported = true;

    @Column(name = "CURRENCY_CODE", unique = true)
    private String code;

    @Column(name = "CURRENCY_NAME", unique = true)
    private String name;

    public Currency() {
    }


    public void setCurrency(java.util.Currency currency) {
        this.currency = currency;
        this.code = currency.getCurrencyCode();
    }


    public String getCode() {
        if (!Objects.equals(currency.getCurrencyCode(), code)) {
            return currency.getCurrencyCode();
        }
        return code;
    }


}
