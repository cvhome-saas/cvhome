package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.catalog.model.product.event.BrandRenamedEvent;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * A brand. Products point at it; the storefront lists the brands present in a category as a filter facet.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "MANUFACTURER", uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CODE"}))
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

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    @Column(name = "SORT_ORDER")
    private Integer order;

    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ManufacturerDescription> descriptions = new HashSet<>();

    public Optional<ManufacturerDescription> description(LanguageCode language) {
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }

    public int getOrder() {
        return order == null ? 0 : order;
    }

    /**
     * The brand's name in every language it has one, keyed by language. Used to tell an actual rename from a save
     * that only touched the code or the sort order — a rename has to rebuild the search document of every product
     * carrying this brand, and that is not work worth doing speculatively.
     */
    public Map<LanguageCode, String> names() {
        return descriptions.stream()
                .filter(d -> d.getLanguageCode() != null && d.getName() != null)
                .collect(Collectors.toMap(ManufacturerDescription::getLanguageCode, ManufacturerDescription::getName,
                        (first, second) -> first));
    }

    /**
     * This brand's name changed, so every product that carries it has a stale search document.
     */
    public Manufacturer renamed() {
        this.registerEvent(BrandRenamedEvent.from(this.id, this.storeMerchantId.getId()));
        return this;
    }
}
