package com.asrevo.cvhome.tenancy.commons.dto;

/**
 * What narrows a listing of organizations. Both fields are optional and they are AND-ed.
 *
 * <p>
 * Mirrors {@link ListManagerStoreQuery}, deliberately: the two screens that use them are the same table twice over,
 * and a filter shape that differed between them would be a difference with no reason behind it.
 * </p>
 *
 * @param term   matched against the name <em>and</em> the contact email, case-insensitively and as a substring.
 *               One parameter rather than two because almost every organization is unnamed — nothing sets a name
 *               at creation — so the console lists many of them by email, and a box that searched only the name
 *               would fail to find the rows on screen
 * @param status {@code ACTIVE}, {@code SUSPENDED} or {@code CLOSED}; null for any
 */
public record ListOrgQuery(String term, OrgStatus status) {
}
