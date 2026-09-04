package com.asrevo.cvhome.tenancy.commons.dto;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The store-creation body, and the payload it becomes on the way to a pod.
 *
 * <p>
 * Three things it does that are easy to break. The placement hint is stripped: the pod runs its own registry and
 * would not know the field. The country is repeated under {@code countryIsoCode}, because merchant's populator
 * reads it under that name and the readable store answers with it. And unknown fields survive the trip, so a theme
 * can grow a setting without this class learning about it.
 * </p>
 */
class CreateStoreRequestTest {

    private static final String STORE_ID = "65f023632bc46470c104b76f";
    private static final String ORG_ID = "21f023932bc66470c104b76f";
    private static final String COUNTRY = "EG";
    private static final String NAME = "shop";
    private static final String ENGLISH = "en";
    private static final String EXTRA_KEY = "heroVideo";
    private static final String EXTRA_VALUE = "https://cdn.example.com/v.mp4";
    private static final String COUNTRY_KEY = "countryIsoCode";
    private static final String POD_KEY = "pod";
    private static final String POD_ID = "507f1f77bcf86cd799439011";

    private static CreateStoreRequest request() {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setName(NAME);
        request.setEmail("owner@example.com");
        request.setPhone("+20100000000");
        request.setTheme("BASIC");
        request.setColorTheme("DEFAULT");
        request.setCurrency("EGP");
        request.setDefaultLanguage(ENGLISH);
        request.setSupportedLanguages(List.of(ENGLISH, "ar"));
        request.setAddress(new CreateStoreRequest.Address(COUNTRY, "Cairo", "11511", null, "1 Main St"));
        return request;
    }

    @Test
    void unknownFieldsSurviveTheTripSoAThemeCanGrowASettingWithoutThisClassKnowing() {
        CreateStoreRequest request = request();
        request.put(EXTRA_KEY, EXTRA_VALUE);

        assertThat(request.any()).containsEntry(EXTRA_KEY, EXTRA_VALUE);
        assertThat(request.toPodPayload(STORE_ID, ORG_ID))
                .containsEntry(EXTRA_KEY, EXTRA_VALUE);
    }

    @Test
    void thePodPayloadCarriesTheIdentityAndRepeatsTheCountryUnderMerchantsName() {
        Map<Object, Object> payload = request().toPodPayload(STORE_ID, ORG_ID);

        assertThat(payload).containsEntry("id", STORE_ID)
                .containsEntry("org", ORG_ID)
                .containsEntry("name", NAME)
                .containsEntry(COUNTRY_KEY, COUNTRY);
    }

    @Test
    void thePlacementHintIsStrippedBecauseThePodRunsItsOwnRegistry() {
        CreateStoreRequest request = request();
        request.setPod(new CreateStoreRequest.PodRef(POD_ID));
        request.put(POD_KEY, "leaked");

        assertThat(request.toPodPayload(STORE_ID, ORG_ID)).doesNotContainKey(POD_KEY);
    }

    @Test
    void anAddresslessRequestReportsANullCountryRatherThanThrowing() {
        CreateStoreRequest request = request();
        request.setAddress(null);

        assertThat(request.toPodPayload(STORE_ID, ORG_ID)).containsEntry(COUNTRY_KEY, null);
    }

    @Test
    void aPreferredPodIsTrimmedAndAbsentWhenBlankOrUnset() {
        CreateStoreRequest request = request();
        assertThat(request.preferredPodId()).isNull();

        request.setPod(new CreateStoreRequest.PodRef("   "));
        assertThat(request.preferredPodId()).isNull();

        request.setPod(new CreateStoreRequest.PodRef(null));
        assertThat(request.preferredPodId()).isNull();

        request.setPod(new CreateStoreRequest.PodRef("  %s  ".formatted(POD_ID)));
        assertThat(request.preferredPodId()).isEqualTo(POD_ID);
    }
}
