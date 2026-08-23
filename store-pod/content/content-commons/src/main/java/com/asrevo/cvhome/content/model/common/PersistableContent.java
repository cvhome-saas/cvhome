package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * Fields every workflow content item carries on write. Type-specific DTOs extend this.
 *
 * <p>
 * {@code version} is the optimistic lock: a {@code PUT} must send the version it read, and a stale one is a 409.
 * The whole {@code translations} list is authoritative — a locale missing from it is deleted.
 * </p>
 */
@Getter
@Setter
public abstract class PersistableContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer version;

    @NotEmpty
    @Size(max = 100)
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug must be lower-case letters, digits and dashes")
    private String slug;

    @Valid
    @NotEmpty
    private List<ContentTranslation> translations = new ArrayList<>();

    private Instant publishAt;

    private Instant unpublishAt;

    private boolean noindex;

    @Size(max = 500)
    private String canonicalUrl;

    private Long ogMediaId;

    private Integer sortOrder;

}
