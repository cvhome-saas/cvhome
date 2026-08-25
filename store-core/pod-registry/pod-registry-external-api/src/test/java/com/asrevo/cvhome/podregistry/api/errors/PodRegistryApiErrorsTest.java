package com.asrevo.cvhome.podregistry.api.errors;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.podregistry.commons.errors.NoEligiblePodException;
import com.asrevo.cvhome.podregistry.commons.errors.PodRegistryErrors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The caller-side error contract: the registry's refusal code becomes a definitive refusal, anything unmapped or
 * unreachable becomes "unavailable", and both name the registry as the service that decided (or did not).
 */
class PodRegistryApiErrorsTest {

    private static final String POD_REGISTRY = "pod-registry";

    private static final String CUSTOM_DETAIL = "custom detail";

    private static final String ORG_PARAM = "orgId";

    private static final String ORG = "o1";

    private static final int UNPROCESSABLE = 422;

    private static final String REASON = "full";

    private static RemoteErrorContext context(String code, String detail, int status) {
        return new RemoteErrorContext(code, detail, Map.of(ORG_PARAM, ORG), List.of(), POD_REGISTRY, status, null,
                null);
    }

    private static RemoteErrorContext refusal(String detail) {
        return context(PodRegistryErrors.NO_ELIGIBLE_POD.code(), detail, UNPROCESSABLE);
    }

    @Test
    void theRefusalCodeMapsToARefusalCarryingTheRegistrysDetail() {
        RemoteServiceException e = PodRegistryApiErrors.CATALOG.find(PodRegistryErrors.NO_ELIGIBLE_POD.code())
                .orElseThrow()
                .create(refusal(CUSTOM_DETAIL));

        assertThat(e).isInstanceOf(PodPlacementRefusedException.class);
        assertThat(e.errorCode()).isEqualTo(PodRegistryErrors.NO_ELIGIBLE_POD);
        assertThat(e.getMessage()).contains(CUSTOM_DETAIL);
        assertThat(e.remoteService()).isEqualTo(POD_REGISTRY);
        assertThat(e.remoteStatus()).isEqualTo(UNPROCESSABLE);
        assertThat(e.params()).containsEntry(ORG_PARAM, ORG);
    }

    @Test
    void aRefusalWithoutDetailGetsADefaultMessage() {
        PodPlacementRefusedException e = PodPlacementRefusedException.from(refusal(null));

        assertThat(e.getMessage()).isNotBlank();
        assertThat(e.remoteCode()).isEqualTo(PodRegistryErrors.NO_ELIGIBLE_POD.code());
    }

    @Test
    void unknownCodesAreNotMapped() {
        assertThat(PodRegistryApiErrors.CATALOG.find("SOMETHING.ELSE")).isEmpty();
        assertThat(PodRegistryApiErrors.CATALOG.find(null)).isEmpty();
    }

    @Test
    @DisplayName("a call that never arrived becomes 'unavailable', with and without a detail from the transport")
    void transportFailuresBecomeUnavailable() {
        RemoteServiceException withDetail = PodRegistryApiErrors.CATALOG.transportFailure()
                .create(context(null, CUSTOM_DETAIL, 0));
        RemoteServiceException withoutDetail = PodRegistryUnavailableException.from(context(null, null, 0));

        assertThat(withDetail).isInstanceOf(PodRegistryUnavailableException.class);
        assertThat(withDetail.errorCode()).isEqualTo(CommonErrors.REMOTE_UNAVAILABLE);
        assertThat(withDetail.getMessage()).contains(CUSTOM_DETAIL);
        assertThat(withDetail.remoteService()).isEqualTo(POD_REGISTRY);
        assertThat(withoutDetail.getMessage()).isNotBlank();
    }

    @Test
    void wrappingAPlainCauseCarriesNoRemoteCode() {
        PodRegistryUnavailableException e = PodRegistryUnavailableException.wrapping(new IOException("boom"));

        assertThat(e.getCause()).isInstanceOf(IOException.class);
        assertThat(e.remoteCode()).isNull();
        assertThat(e.remoteService()).isEqualTo(POD_REGISTRY);
    }

    @Test
    void wrappingACodedCauseKeepsItsCodeAndParams() {
        NoEligiblePodException cause = NoEligiblePodException.of(ORG, REASON);

        PodRegistryUnavailableException e = PodRegistryUnavailableException.wrapping(cause);

        assertThat(e.remoteCode()).isEqualTo(PodRegistryErrors.NO_ELIGIBLE_POD.code());
        assertThat(e.params()).containsEntry("reason", REASON);
    }

    @Test
    void wrappingARemoteCauseKeepsItsStatus() {
        PodPlacementRefusedException cause = PodPlacementRefusedException.from(refusal(CUSTOM_DETAIL));

        PodRegistryUnavailableException e = PodRegistryUnavailableException.wrapping(cause);

        assertThat(e.remoteStatus()).isEqualTo(UNPROCESSABLE);
        assertThat(e.remoteCode()).isEqualTo(PodRegistryErrors.NO_ELIGIBLE_POD.code());
    }

}
