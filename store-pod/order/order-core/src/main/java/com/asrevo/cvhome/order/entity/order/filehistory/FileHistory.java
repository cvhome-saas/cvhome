package com.asrevo.cvhome.order.entity.order.filehistory;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FILE_HISTORY",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "STORE_MERCHANT_ID", "FILE_ID" }) })
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
	@AttributeOverrides(@AttributeOverride(name = "storeMerchantId",
			column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
	private StoreMerchantId storeMerchantId;

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

	public FileHistory() {
	}

}
