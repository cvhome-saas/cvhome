package com.asrevo.cvhome.store.core.entity.system.optin;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * Optin defines optin campaigns for the system.
 *
 * @author carlsamson
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CUSTOMER_OPTIN", uniqueConstraints =
@UniqueConstraint(columnNames = {"EMAIL", "OPTIN_ID"}))
@Getter
@Setter
public class CustomerOptin extends SalesManagerEntity<Long, CustomerOptin> implements Serializable {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_OPTIN_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "CUST_OPT_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "OPTIN_DATE")
    private Date optinDate;


    @ManyToOne(targetEntity = Optin.class)
    @JoinColumn(name = "OPTIN_ID")
    private Optin optin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @Column(name = "FIRST")
    private String firstName;

    @Column(name = "LAST")
    private String lastName;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Column(name = "VALUE", columnDefinition = "text")
    private String value;

}
