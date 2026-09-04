package com.asrevo.cvhome.uaa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Where the seller console renders the pages uaa used to render itself.
 *
 * <p>
 * uaa has two front doors and this describes the second one. Reached on its own host it serves its own admin SPA,
 * sign-in page included, which is the door a platform administrator uses. Reached through store-core-gateway under
 * {@link #pathPrefix} it is behind the console, on the console's origin, and the console owns every page somebody
 * sees before they are signed in — so uaa hands the browser over instead of rendering.
 * </p>
 *
 * <p>
 * There is no host here on purpose. The console answers on {@code gateway.com}, {@code www.gateway.com} and
 * {@code console-ui.gateway.com}, and a hand-off has to come back to whichever one the person started on, so the
 * origin is read off the request. The links that leave in an email have no request to read and are configured
 * separately, in {@code com.asrevo.cvhome.uaa.links}.
 * </p>
 */
@ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.console")
@Getter
@Setter
public class ConsoleProperties {

    /**
     * The path store-core-gateway forwards to uaa under, and the whole hand-off switch: a request whose context
     * path is this arrived through the console, and anything else is somebody on uaa's own host. Empty turns the
     * hand-off off and gives every caller uaa's own pages back.
     */
    private String pathPrefix = "";

    /** The console route that renders the sign-in form. */
    private String signInPage = "/sign-in";

    /** True when the hand-off is configured at all. */
    public boolean isEnabled() {
        return !pathPrefix.isEmpty();
    }

}
