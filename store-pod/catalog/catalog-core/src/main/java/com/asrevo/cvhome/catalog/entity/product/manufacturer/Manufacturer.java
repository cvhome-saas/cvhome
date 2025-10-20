package com.asrevo.cvhome.catalog.entity.product.manufacturer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "MANUFACTURER", uniqueConstraints = @UniqueConstraint(columnNames = { "STORE_MERCHANT_ID", "CODE" }))
@Getter
@Setter
public class Manufacturer extends SalesManagerEntity<Long, Manufacturer> implements Auditable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "MANUFACTURER_ID", unique = true, nullable = false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT", pkColumnValue = "MANUFACTURER_SEQ_NEXT_VAL",
			allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
			initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;

	@Embedded
	private AuditSection auditSection = new AuditSection();

	@OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<ManufacturerDescription> descriptions = new HashSet<>();

	@Column(name = "MANUFACTURER_IMAGE")
	private String image;

	@Column(name = "SORT_ORDER")
	private Integer order = 0;

	@Embedded
	@AttributeOverrides(@AttributeOverride(name = "storeMerchantId",
			column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
	private StoreMerchantId storeMerchantId;

	@NotEmpty
	@Column(name = "CODE", length = 100, nullable = false)
	private String code;

	public Manufacturer() {
	}

}
