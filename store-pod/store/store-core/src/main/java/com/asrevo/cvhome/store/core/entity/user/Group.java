package com.asrevo.cvhome.store.core.entity.user;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SM_GROUP", indexes = {
        @Index(name = "SM_GROUP_GROUP_TYPE", columnList = "GROUP_TYPE")})
@Getter
@Setter
public class Group extends SalesManagerEntity<Integer, Group> implements Auditable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "GROUP_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "GROUP_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Integer id;
    @Column(name = "GROUP_TYPE")
    @Enumerated(value = EnumType.STRING)
    private GroupType groupType;
    @NotEmpty
    @Column(name = "GROUP_NAME", unique = true)
    private String groupName;
    @JsonIgnore
    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "PERMISSION_GROUP",
            joinColumns = @JoinColumn(name = "GROUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID")
    )
    private Set<Permission> permissions = new HashSet<Permission>();
    @Embedded
    private AuditSection auditSection = new AuditSection();

    public Group() {

    }

}
