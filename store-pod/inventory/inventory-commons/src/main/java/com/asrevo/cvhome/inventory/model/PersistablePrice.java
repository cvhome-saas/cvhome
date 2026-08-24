package com.asrevo.cvhome.inventory.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * The price a merchant sets on a sku. The special amount is optional; when set, it applies between the two dates,
 * either of which may be left open.
 */
public record PersistablePrice(@NotNull @DecimalMin("0") BigDecimal amount,
                               @DecimalMin("0") BigDecimal specialAmount,
                               LocalDate specialStartDate,
                               LocalDate specialEndDate) implements Serializable {
}
