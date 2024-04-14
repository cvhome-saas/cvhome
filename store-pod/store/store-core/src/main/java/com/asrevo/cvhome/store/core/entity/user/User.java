package com.asrevo.cvhome.store.core.entity.user;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.CredentialsReset;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User management
 *
 * @author carlsamson
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "USERS",
        indexes = {@Index(name = "USR_NAME_IDX", columnList = "ADMIN_NAME")},
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"MERCHANT_ID", "ADMIN_NAME"}))
@Getter
@Setter
public class User extends SalesManagerEntity<Long, User> implements Auditable {


    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "USER_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;
    @NotEmpty
    @Column(name = "ADMIN_NAME", length = 100)
    private String adminName;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinTable(name = "USER_GROUP", joinColumns = {
            @JoinColumn(name = "USER_ID", nullable = false, updatable = false, insertable = false)}
            ,
            inverseJoinColumns = {@JoinColumn(name = "GROUP_ID",
                    nullable = false, updatable = false, insertable = false)}
    )
    @Cascade({
            org.hibernate.annotations.CascadeType.DETACH,
            org.hibernate.annotations.CascadeType.LOCK,
            org.hibernate.annotations.CascadeType.REFRESH,
            org.hibernate.annotations.CascadeType.REPLICATE

    })
    private List<Group> groups = new ArrayList<Group>();
    @NotEmpty
    @Email
    @Column(name = "ADMIN_EMAIL")
    private String adminEmail;
    @NotEmpty
    @Column(name = "ADMIN_PASSWORD", length = 80)
    private String adminPassword;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;
    @Column(name = "ADMIN_FIRST_NAME")
    private String firstName;
    @Column(name = "ACTIVE")
    private boolean active = true;
    @Column(name = "ADMIN_LAST_NAME")
    private String lastName;
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Language.class)
    @JoinColumn(name = "LANGUAGE_ID")
    private Language defaultLanguage;
    @Column(name = "ADMIN_Q1")
    private String question1;
    @Column(name = "ADMIN_Q2")
    private String question2;
    @Column(name = "ADMIN_Q3")
    private String question3;
    @Column(name = "ADMIN_A1")
    private String answer1;
    @Column(name = "ADMIN_A2")
    private String answer2;
    @Column(name = "ADMIN_A3")
    private String answer3;
    @Embedded
    private AuditSection auditSection = new AuditSection();
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_ACCESS")
    private Date lastAccess;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LOGIN_ACCESS")
    private Date loginTime;
    @Embedded
    private CredentialsReset credentialsResetRequest = null;

    public User() {

    }

    public User(String userName, String password, String email) {

        this.adminName = userName;
        this.adminPassword = password;
        this.adminEmail = email;
    }

}
