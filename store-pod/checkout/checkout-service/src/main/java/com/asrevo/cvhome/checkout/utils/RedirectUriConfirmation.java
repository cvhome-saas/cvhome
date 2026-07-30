package com.asrevo.cvhome.checkout.utils;

import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.commons.domain.LanguageCode;

public record RedirectUriConfirmation(String domain,LanguageCode language) {

    public String success() {
        return UriComponentsBuilder.fromUriString(domain).path("/").path(language.code()).path("/checkout/success")
                .toUriString();
    }

    public String cancel() {
        return UriComponentsBuilder.fromUriString(domain).path("/").path(language.code()).path("/checkout/cancel").toUriString();
    }
}
