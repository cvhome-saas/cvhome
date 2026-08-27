package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import com.asrevo.cvhome.catalog.entity.type.TsVectorType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.Getter;
import lombok.Setter;

/**
 * The searchable form of one product in one language.
 *
 * <p>
 * Read-only from Java's point of view: the rows are written by {@code catalog.refresh_product_search_index}, which
 * the outbox handler calls. Nothing here is ever mapped back into a product.
 * </p>
 *
 * <p>
 * The entity exists so a JPA {@code Specification} can name {@code searchDocument} in a predicate. It is only ever
 * the root of a correlated {@code exists} subquery that selects a literal — putting a {@code tsvector} in a select
 * list would mean fetching the whole document for every row of every page.
 * </p>
 */
@Entity
@Immutable
@Table(name = "PRODUCT_SEARCH_INDEX")
@IdClass(ProductSearchIndexId.class)
@Getter
@Setter
public class ProductSearchIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    /**
     * A plain string, not a {@code LanguageCode}: JPA forbids an {@code AttributeConverter} on an {@code @Id},
     * and this table is a read-only projection rather than a domain aggregate, so the value object buys nothing
     * here. Callers pass {@code language.code()}.
     */
    @Id
    @Column(name = "LANGUAGE_CODE", nullable = false, length = 6)
    private String languageCode;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId store;

    /**
     * The product name, run through {@code catalog.search_normalize}. Trigram-indexed, and read only on the
     * fallback path when the tsquery matched nothing and we are looking for a near miss.
     */
    @Column(name = "NAME_NORMALIZED")
    private String nameNormalized;

    /**
     * {@code columnDefinition} is not decoration: the type maps to {@code Types.OTHER}, which Hibernate cannot
     * turn into a column type on its own, and {@code ddl-auto: update} inspects this table like any other even
     * though {@code schema.sql} is what actually creates it.
     */
    @Type(TsVectorType.class)
    @Column(name = "SEARCH_DOCUMENT", columnDefinition = "tsvector")
    private String searchDocument;

    @Column(name = "INDEXED_AT")
    private Instant indexedAt;
}
