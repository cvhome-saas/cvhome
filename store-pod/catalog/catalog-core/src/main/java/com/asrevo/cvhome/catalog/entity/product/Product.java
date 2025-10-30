package com.asrevo.cvhome.catalog.entity.product;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PRODUCT", uniqueConstraints = @UniqueConstraint(columnNames = { "STORE_MERCHANT_ID", "SKU" }))
@Getter
@Setter
@FilterDef(name = "productDescriptionLanguageCodeFilter",
		parameters = @ParamDef(name = "languageCode", type = String.class),
		defaultCondition = "LANGUAGE_CODE = :languageCode")
public class Product extends SalesManagerEntity<Long, Product> implements Auditable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PRODUCT_ID", unique = true, nullable = false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_SEQ_NEXT_VAL",
			allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
			initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;

	@Embedded
	private AuditSection auditSection = new AuditSection();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product")
	@Filter(name = "productDescriptionLanguageCodeFilter")
	private Set<ProductDescription> descriptions = new HashSet<>();

	/**
	 * Inventory
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product")
	private Set<ProductAvailability> availabilities = new HashSet<>();

	/**
	 * Attributes of a product Decorates the product with additional properties
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product")
	private Set<ProductAttribute> attributes = new HashSet<>();

	/**
	 * Default product images
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "product")
	// cascade is set to remove because product save requires logic to create physical
	// image first
	// and then save the image id in the database, cannot be done in cascade
	private Set<ProductImage> images = new HashSet<>();

	/**
	 * Related items / product groups
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product")
	private Set<ProductRelationship> relationships = new HashSet<>();

	@Embedded
	@AttributeOverrides(@AttributeOverride(name = "storeMerchantId",
			column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
	private StoreMerchantId store;

	/**
	 * Product to category
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
	@JoinTable(name = "PRODUCT_CATEGORY", joinColumns = { @JoinColumn(name = "PRODUCT_ID") },
			inverseJoinColumns = { @JoinColumn(name = "CATEGORY_ID") })
	@Cascade({ org.hibernate.annotations.CascadeType.DETACH, org.hibernate.annotations.CascadeType.LOCK,
			org.hibernate.annotations.CascadeType.REFRESH, org.hibernate.annotations.CascadeType.REPLICATE })
	private Set<Category> categories = new HashSet<>();

	/**
	 * Product variants Decorates the product with variants
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product")
	private Set<ProductVariant> variants = new HashSet<>();

	@Column(name = "DATE_AVAILABLE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAvailable = new Date();

	@Column(name = "AVAILABLE")
	private boolean available = true;

	@Column(name = "PREORDER")
	private boolean preOrder = false;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
	@JoinColumn(name = "MANUFACTURER_ID")
	private Manufacturer manufacturer;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
	@JoinColumn(name = "PRODUCT_TYPE_ID")
	private ProductType type;

	@Column(name = "PRODUCT_VIRTUAL")
	private boolean productVirtual = false;

	@Column(name = "PRODUCT_SHIP")
	private boolean productShipeable = false;

	@Column(name = "PRODUCT_FREE")
	private boolean productIsFree;

	@Column(name = "PRODUCT_LENGTH")
	private BigDecimal productLength;

	@Column(name = "PRODUCT_WIDTH")
	private BigDecimal productWidth;

	@Column(name = "PRODUCT_HEIGHT")
	private BigDecimal productHeight;

	@Column(name = "PRODUCT_WEIGHT")
	private BigDecimal productWeight;

	@Column(name = "REVIEW_AVG")
	private BigDecimal productReviewAvg;

	@Column(name = "REVIEW_COUNT")
	private Integer productReviewCount;

	@Column(name = "QUANTITY_ORDERED")
	private Integer productOrdered;

	@Column(name = "SORT_ORDER")
	private Integer sortOrder = 0;

	@NotEmpty
	@Pattern(regexp = "^[a-zA-Z0-9_-]*$")
	@Column(name = "SKU")
	private String sku;

	/**
	 * External system reference SKU/ID
	 */
	@Column(name = "REF_SKU")
	private String refSku;

	@Column(name = "COND")
	private ProductCondition condition;

	/**
	 * RENTAL ADDITIONAL FIELDS
	 */
	@Column(name = "RENTAL_STATUS")
	private RentalStatus rentalStatus;

	@Column(name = "RENTAL_DURATION")
	private Integer rentalDuration;

	@Column(name = "RENTAL_PERIOD")
	private Integer rentalPeriod;

	public Product() {
	}

	public ProductDescription getProductDescription() {
		if (this.getDescriptions() != null && !this.getDescriptions().isEmpty()) {
			return this.getDescriptions().iterator().next();
		}
		return null;
	}

	public ProductImage getProductImage() {
		ProductImage productImage = null;
		if (this.getImages() != null && !this.getImages().isEmpty()) {
			for (ProductImage image : this.getImages()) {
				productImage = image;
				if (productImage.isDefaultImage()) {
					break;
				}
			}
		}
		return productImage;
	}

}
