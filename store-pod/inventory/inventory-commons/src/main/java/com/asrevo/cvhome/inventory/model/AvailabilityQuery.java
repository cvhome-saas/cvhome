package com.asrevo.cvhome.inventory.model;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of the POST form of the availability read — for callers whose sku list (a PDP's whole variant matrix) is too
 * long to sit comfortably in a GET query string. Same answer as the GET: skus with no record are absent.
 */
public record AvailabilityQuery(@NotNull @Size(min = 1, max = 500) List<String> skus) implements Serializable {
}
