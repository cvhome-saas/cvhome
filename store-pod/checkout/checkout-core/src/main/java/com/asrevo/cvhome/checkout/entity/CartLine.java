package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.checkout.entity.converter.OptionLabelsConverter;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.SkuConverter;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "CART_LINE", uniqueConstraints = @UniqueConstraint(name = "UK_CART_LINE_SKU",
        columnNames = {"CART_ID", "SKU"}))
@Getter
@Setter
public class CartLine extends SalesManagerEntity<Long, CartLine> implements Auditable {

    /** How long a line trusts what the catalogue said before a read asks it again. */
    public static final Duration SNAPSHOT_FOR = Duration.ofDays(1);

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "LINE_ID")
    @SequenceGenerator(name = "cart_line_seq", sequenceName = "cart_line_seq",
            allocationSize = SchemaConstant.ID_ALLOCATION_SIZE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_line_seq")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CART_ID", nullable = false)
    private Cart cart;

    @Column(name = "SKU", nullable = false)
    @Convert(converter = SkuConverter.class)
    private Sku sku;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    /**
     * What the catalogue said about the sku when the line was added, so a cart read asks inventory for the live
     * price and stock and the catalogue for nothing: in the 2026-09-14 load test the cart's catalog read was
     * catalog's largest cost, made again on every read of every cart. Null on a line written before this column;
     * refreshed after {@link #SNAPSHOT_FOR}.
     */
    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "FRIENDLY_URL")
    private String friendlyUrl;

    @Column(name = "IMAGE_URL", length = 1024)
    private String imageUrl;

    @Column(name = "OPTION_LABELS", length = 2000)
    @Convert(converter = OptionLabelsConverter.class)
    private List<OptionLabel> optionLabels = new ArrayList<>();

    @Column(name = "CATALOG_AVAILABLE")
    private Boolean catalogAvailable;

    @Column(name = "SNAPSHOT_AT")
    private Instant snapshotAt;

    public CartLine() {
    }

    public CartLine(Cart cart, Sku sku, int quantity) {
        this.cart = cart;
        this.sku = sku;
        this.quantity = quantity;
    }

    /** Keeps what the catalogue said about the sku, as of {@code now}. */
    public void remember(Long product, String name, String slug, String image, boolean available,
                         List<OptionLabel> labels, Instant now) {
        this.productId = product;
        this.productName = name;
        this.friendlyUrl = slug;
        this.imageUrl = image;
        this.catalogAvailable = available;
        this.optionLabels = new ArrayList<>(labels == null ? List.of() : labels);
        this.snapshotAt = now;
    }

    /** Whether the line still carries a snapshot a read may trust at {@code now}. */
    public boolean remembers(Instant now) {
        return snapshotAt != null && catalogAvailable != null && !snapshotAt.plus(SNAPSHOT_FOR).isBefore(now);
    }
}
