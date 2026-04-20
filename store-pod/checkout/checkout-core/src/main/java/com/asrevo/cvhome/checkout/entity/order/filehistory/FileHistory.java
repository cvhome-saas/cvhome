package com.asrevo.cvhome.checkout.entity.order.filehistory;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FILE_HISTORY",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "FILE_ID"})})
@Getter
@Setter
public class FileHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1321251632883237664L;

    @Id
    @Column(name = "FILE_HISTORY_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "FILE_HISTORY_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "FILE_ID")
    private Long fileId;

    @Column(name = "FILESIZE", nullable = false)
    private Integer filesize;

    @Column(name = "DATE_ADDED", length = 0, nullable = false)
    private Instant dateAdded;

    @Column(name = "DATE_DELETED", length = 0)
    private Instant dateDeleted;

    @Column(name = "ACCOUNTED_DATE", length = 0)
    private Instant accountedDate;

    @Column(name = "DOWNLOAD_COUNT", nullable = false)
    private Integer downloadCount;

}
