package com.asrevo.cvhome.store.core.entity.user;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PERMISSION")
@Getter
@Setter
public class Permission extends SalesManagerEntity<Integer, Permission> implements Auditable {


    @Serial
    private static final long serialVersionUID = 813468140197420748L;

    @Id
    @Column(name = "PERMISSION_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "PERMISSION_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Integer id;
    @NotEmpty
    @Column(name = "PERMISSION_NAME", unique = true)
    private String permissionName;
    @ManyToMany(mappedBy = "permissions")
    private List<Group> groups = new ArrayList<Group>();
    @Embedded
    private AuditSection auditSection = new AuditSection();

    public Permission() {

    }

    public Permission(String permissionName) {
        this.permissionName = permissionName;
    }


}
