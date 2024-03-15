package com.asrevo.cvhome.product.commons.domain;

import java.net.MalformedURLException;
import java.net.URL;

public record ImageLink(String imageLink) {
    public ImageLink {
        if (imageLink == null || imageLink.isEmpty()) throw new RuntimeException("image link should be valid");
        try {
            new URL(imageLink);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
