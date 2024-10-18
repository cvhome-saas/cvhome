package com.asrevo.cvhome.store.core.entity.order.filehistory;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "FILE_HISTORY",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"MERCHANT_ID", "FILE_ID"})})
@Getter
@Setter
public class FileHistory implements Serializable {
    @Serial private static final long serialVersionUID = 1321251632883237664L;

    @Id
    @Column(name = "FILE_HISTORY_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "FILE_HISTORY_ID_NEXT_VALUE",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(targetEntity = MerchantStore.class)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore store;

    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "FILESIZE", nullable = false)
    private Integer filesize;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_ADDED", length = 0, nullable = false)
    private Date dateAdded;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_DELETED", length = 0)
    private Date dateDeleted;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ACCOUNTED_DATE", length = 0)
    private Date accountedDate;

    @Column(name = "DOWNLOAD_COUNT", nullable = false)
    private Integer downloadCount;

    public FileHistory() {}
}
