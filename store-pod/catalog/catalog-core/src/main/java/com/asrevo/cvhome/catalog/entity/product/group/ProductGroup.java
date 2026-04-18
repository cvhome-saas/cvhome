package com.asrevo.cvhome.catalog.entity.product.group;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PRODUCT_GROUP", uniqueConstraints = @UniqueConstraint(columnNames = { "STORE_MERCHANT_ID", "CODE" }))
@Getter
@Setter
public class ProductGroup extends SalesManagerEntity<Long, ProductGroup> implements Auditable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PRODUCT_GROUP_ID", unique = true, nullable = false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_GROUP_SEQ_NEXT_VAL",
			allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
			initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;

	@Embedded
	private AuditSection auditSection = new AuditSection();

	@Embedded
	@AttributeOverrides(@AttributeOverride(name = "storeMerchantId",
			column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
	private StoreMerchantId storeMerchantId;

	@NotEmpty
	@Column(name = "CODE", length = 100, nullable = false)
	private String code;

	@Column(name = "ACTIVE")
	private boolean active = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_PRODUCT_ID")
	private Product parentProduct;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "PRODUCT_GROUP_PRODUCT",
			joinColumns = @JoinColumn(name = "PRODUCT_GROUP_ID", referencedColumnName = "PRODUCT_GROUP_ID"),
			inverseJoinColumns = @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID"))
	private List<Product> products = new ArrayList<>();

	@Valid
	@OneToMany(mappedBy = "productGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<ProductGroupDescription> descriptions = new HashSet<>();

	public ProductGroupDescription getDescription() {
		if (descriptions != null && !descriptions.isEmpty()) {
			return descriptions.iterator().next();
		}
		return null;
	}

}
