package com.asrevo.cvhome.podregistry.commons;

/**
 * Who may be placed on a pod.
 *
 * <p>
 * A {@link #PRIVATE} pod belongs to one organization and only that organization's stores may land on it; the
 * {@code org_id} column carries which. {@link #PUBLIC} pods are shared and carry no org.
 * </p>
 *
 * <p>
 * This exists as its own column rather than being inferred from {@code org_id != null} so an operator can hold a
 * pod out of public rotation without inventing an owner for it.
 * </p>
 */
public enum PodVisibility {

    PUBLIC,
    PRIVATE

}
