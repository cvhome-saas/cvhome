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
@Table(name = "OPTIN", uniqueConstraints =
@UniqueConstraint(columnNames = {"MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class Optin extends SalesManagerEntity<Long, Optin> implements Serializable {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "OPTIN_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "OPTIN_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_DATE")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private OptinType optinType;

    @ManyToOne(targetEntity = MerchantStore.class)
    @JoinColumn(name = "MERCHANT_ID")
    private MerchantStore merchant;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

}
