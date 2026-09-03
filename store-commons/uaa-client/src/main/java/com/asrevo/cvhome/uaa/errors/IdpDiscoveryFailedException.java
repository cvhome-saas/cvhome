package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;

/**
 * The provider did not answer: its discovery document or an endpoint could not be fetched. A third party failed,
 * so this is an {@link ExternalProviderException}: the provider's own status travels as an extension and never
 * becomes ours.
 */
public class IdpDiscoveryFailedException extends ExternalProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IdpDiscoveryFailedException(ErrorPayload payload, Throwable cause, String provider, String providerCode,
                                          int providerStatus) {
        super(payload, cause, provider, providerCode, providerStatus);
    }

    public static IdpDiscoveryFailedException of(String alias, String url, int status, Throwable cause) {
        return ExternalProviderException.of(UaaErrors.IDP_DISCOVERY_FAILED, IdpDiscoveryFailedException::new)
                .detail("Provider %s did not answer at %s.", alias, url)
                .param("url", url)
                .provider(alias)
                .providerStatus(status)
                .cause(cause)
                .build();
    }

}
