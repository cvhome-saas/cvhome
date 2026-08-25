/**
 * The flat rows the platform's aggregate queries select.
 *
 * <p>
 * Separate from {@code billing.commons.dto.admin} because the two answer different questions. A projection is bound
 * by Spring Data JDBC one column at a time, so it can hold only what a column can hold — a {@link
 * com.asrevo.cvhome.billing.commons.Money} is a currency <em>and</em> an amount and cannot be bound from two columns
 * at once. The mappers in {@code billing.mappers} are what turn a row into the DTO that ships.
 * </p>
 */
package com.asrevo.cvhome.billing.repository.projection;
