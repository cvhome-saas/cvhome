package com.asrevo.cvhome.tenancy.commons.dto;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
 * <h2>Why merchant's required fields are typed here</h2>
 *
 * <p>
 * Tenancy owns only {@link #name} and {@link #pod}. Everything else on this request belongs to <em>merchant's</em>
 * store model, and an earlier revision left all of it in {@link #getAdditional()} on the argument that duplicating
 * merchant's model here means two definitions to keep in step.
 * </p>
 *
 * <p>
 * That argument cost more than it saved. Creating a store is a two-hop, <strong>asynchronous</strong> operation:
 * this endpoint answers {@code 200} as soon as tenancy's row exists, and the pod create runs later off the outbox.
 * With nothing validated here, a body missing {@code email} was accepted, a store row was created, and the pod
 * refused it minutes later with a 400 that {@code StoreProvisioningService} could only record as
 * {@code FAILED_PROVISIONING}. The caller was told the store was being built, and then that it had failed, and at
 * no point which field was wrong. The console-ui create form shipped posting four fields for exactly that reason —
 * nothing rejected it.
 * </p>
 *
 * <p>
 * So the fields the pod <em>refuses without</em> are typed and validated here, where the failure can still be a
 * synchronous {@code 400} with field errors the form binds. The duplication is real and is the price: these
 * constraints mirror {@code MerchantStoreDetails}'s {@code @NotNull}s and {@code merchant.merchant_store}'s NOT
 * NULL columns, and a change to either has to be applied here too. Anything the pod merely tolerates —
 * {@code inBusinessSince}, {@code dimension}, {@code weight}, {@code useCache}, {@code template} — is deliberately
 * left untyped in {@link #getAdditional()} and forwarded as it arrives.
 * </p>
 *
 * <p>
 * The wire shape is unchanged: the console still posts a flat merchant store object, and the unknown fields land
 * in the map.
 * </p>
 */
@Getter
@Setter
public class CreateStoreRequest {

    private static final String NAME = "name";

    private static final String ID = "id";

    private static final String ORG = "org";

    private static final String POD = "pod";

    private static final String EMAIL = "email";

    private static final String PHONE = "phone";

    private static final String THEME = "theme";

    private static final String COLOR_THEME = "colorTheme";

    private static final String CURRENCY = "currency";

    private static final String DEFAULT_LANGUAGE = "defaultLanguage";

    private static final String SUPPORTED_LANGUAGES = "supportedLanguages";

    private static final String ADDRESS = "address";

    private static final String COUNTRY_ISO_CODE = "countryIsoCode";

    @NotBlank
    private String name;

    /** An operator's choice of pod. Honoured only if the registry finds it eligible; null means no preference. */
    private PodRef pod;

    /** {@code MerchantStoreDetails.email} is {@code @NotNull} and {@code store_email} is NOT NULL. */
    @NotBlank
    @Email
    private String email;

    /** {@code MerchantStoreDetails.phone} is {@code @NotNull}. */
    @NotBlank
    private String phone;

    /** {@code merchant_store.theme} is NOT NULL, and constrained to the {@code Theme} enum by a check. */
    @NotBlank
    private String theme;

    /** {@code merchant_store.color_theme} is NOT NULL, and constrained to the {@code ColorTheme} enum. */
    @NotBlank
    private String colorTheme;

    /** {@code merchant_store.currency_id} is NOT NULL. */
    @NotBlank
    private String currency;

    /**
     * {@code merchant_store.language_code} is NOT NULL, and
     * {@code PersistableMerchantStorePopulator.applyLanguages} dereferences this without a null check — omitting it
     * used to reach the pod as a 500 rather than a validation failure.
     */
    @NotBlank
    private String defaultLanguage;

    /** Dereferenced unguarded by the same populator, and the source of the store's {@code merchant_language} rows. */
    @NotEmpty
    private List<String> supportedLanguages;

    /** The trading address. See {@link Address} for which parts merchant refuses without. */
    @NotNull
    @Valid
    private Address address;

    /**
     * Everything else the caller sent, forwarded to the pod as-is.
     *
     * <p>
     * {@code @JsonAnySetter} is what keeps the wire shape flat while the fields above are typed.
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
     * mattered. Every typed field has to be written back into the map, because typing it took it out of
     * {@link #additional}.
     * </p>
     */
    public Map<Object, Object> toPodPayload(String storeId, String orgId) {
        Map<Object, Object> payload = new HashMap<>(additional);
        payload.put(NAME, name);
        payload.put(ID, storeId);
        payload.put(ORG, orgId);
        payload.put(EMAIL, email);
        payload.put(PHONE, phone);
        payload.put(THEME, theme);
        payload.put(COLOR_THEME, colorTheme);
        payload.put(CURRENCY, currency);
        payload.put(DEFAULT_LANGUAGE, defaultLanguage);
        payload.put(SUPPORTED_LANGUAGES, supportedLanguages);
        payload.put(ADDRESS, address);
        // The populator reads the country off the address; countryIsoCode is the same value under merchant's own
        // name for it, and the readable store answers with that one.
        payload.put(COUNTRY_ISO_CODE, address == null ? null : address.country());
        // The pod has its own registry; it has no use for our placement hint and would not know the field.
        payload.remove(POD);
        return payload;
    }

    /** A pod reference as the console sends it — {@code {"pod": {"id": "..."}}}. */
    public record PodRef(String id) {
    }

    /**
     * The store's registered address, in merchant's {@code PersistableBaseAddress} shape.
     *
     * <p>
     * {@code country}, {@code city} and {@code postalCode} are all required, and the reason is not the
     * same for each — which is why the DDL alone is not enough to work this out. {@code country} backs a
     * NOT NULL column; {@code city} and {@code postalCode} back <em>nullable</em> columns but carry
     * {@code @NotEmpty} on the {@code MerchantStore} <strong>entity</strong>, so Hibernate refuses them
     * at persist time with a {@code ConstraintViolationException} — a 500, not a 400, because it fires
     * below the layer that renders validation failures.
     * </p>
     *
     * <p>
     * An earlier revision of this record trusted the column definitions and left both optional. The
     * store row was created, the pod rejected it, and the console reported a failed store with
     * {@code COMMON.INTERNAL_ERROR} against it — which is exactly the shape of failure this class
     * exists to prevent, arrived at from the other direction. Read the entity, not the schema.
     * </p>
     *
     * <p>
     * {@code stateProvince} and the street {@code address} carry no constraint on either and stay
     * optional.
     * </p>
     */
    public record Address(@NotBlank String country, @NotBlank String city, @NotBlank String postalCode,
                          String stateProvince, String address) {
    }

}
