package com.asrevo.cvhome.tenancy.commons.dto;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * What a caller sends to create a store.
 *
 * <p>
 * This replaces {@code Map<Object, Object>}, which was threaded through six signatures and serialized into the
 * outbox. The map was read with {@code request.get("name").toString()} — an NPE for anyone who omitted a name —
 * and nothing in any signature said what a caller was supposed to send.
 * </p>
 *
 * <p>
 * <strong>Only the two fields tenancy actually uses are typed.</strong> The rest of the body — address, email,
 * phone, currency, units, the whole of merchant's store model — is collected into {@link #getAdditional()} and
 * forwarded untouched. Tenancy needs the name (it owns the row and the uniqueness constraint) and the preferred
 * pod (it asks the registry for placement); everything else belongs to merchant, and duplicating merchant's model
 * here would mean two definitions of a store that have to be kept in step forever.
 * </p>
 *
 * <p>
 * The wire shape is unchanged: the console still posts a flat merchant store object, and the unknown fields land
 * in the map. That is what lets this be typed without rewriting the create form.
 * </p>
 */
@Getter
@Setter
public class CreateStoreRequest {

    private static final String NAME = "name";

    private static final String ID = "id";

    private static final String ORG = "org";

    private static final String POD = "pod";

    @NotBlank
    private String name;

    /** An operator's choice of pod. Honoured only if the registry finds it eligible; null means no preference. */
    private PodRef pod;

    /**
     * Everything else the caller sent, forwarded to the pod as-is.
     *
     * <p>
     * {@code @JsonAnySetter} is what keeps the wire shape flat while the two fields above are typed.
     * </p>
     */
    @JsonIgnore
    private final Map<String, Object> additional = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        additional.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> any() {
        return additional;
    }

    /** The preferred pod's id, or null. */
    @JsonIgnore
    public String preferredPodId() {
        if (Objects.isNull(pod) || Objects.isNull(pod.id()) || pod.id().isBlank()) {
            return null;
        }
        return pod.id().trim();
    }

    /**
     * The body to send to the pod: everything the caller sent, plus the ids tenancy assigned.
     *
     * <p>
     * Built here rather than in a mapper so that what crosses the wire to merchant is defined next to what came
     * in — the two used to be a map going through three layers, and it was not obvious anywhere which keys
     * mattered.
     * </p>
     */
    public Map<Object, Object> toPodPayload(String storeId, String orgId) {
        Map<Object, Object> payload = new HashMap<>(additional);
        payload.put(NAME, name);
        payload.put(ID, storeId);
        payload.put(ORG, orgId);
        // The pod has its own registry; it has no use for our placement hint and would not know the field.
        payload.remove(POD);
        return payload;
    }

    /** A pod reference as the console sends it — {@code {"pod": {"id": "..."}}}. */
    public record PodRef(String id) {
    }

}
