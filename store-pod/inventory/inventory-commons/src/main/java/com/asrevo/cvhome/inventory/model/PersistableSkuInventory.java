package com.asrevo.cvhome.inventory.model;

import java.io.Serializable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.commons.domain.Sku;

/**
 * One element of the bulk upsert: the sku plus the same body the single-sku {@code PUT /{sku}} takes. Used when the
 * console saves a whole variant matrix in one call instead of one round-trip per sku.
 */
public record PersistableSkuInventory(@NotEmpty @Pattern(regexp = Sku.FORMAT) String sku,
                                      @NotNull @Valid PersistableInventory inventory) implements Serializable {
}
