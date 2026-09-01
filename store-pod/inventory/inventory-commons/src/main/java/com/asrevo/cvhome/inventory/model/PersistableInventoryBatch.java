package com.asrevo.cvhome.inventory.model;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of the bulk upsert: a whole variant matrix in one call. Capped so one request cannot write an unbounded
 * number of rows; the console saves at most one product's variants at a time.
 */
public record PersistableInventoryBatch(
        @NotNull @Size(min = 1, max = 200) @Valid List<PersistableSkuInventory> entries) implements Serializable {
}
