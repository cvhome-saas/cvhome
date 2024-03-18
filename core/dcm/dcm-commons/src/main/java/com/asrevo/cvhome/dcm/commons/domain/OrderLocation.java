package com.asrevo.cvhome.dcm.commons.domain;

import java.net.MalformedURLException;
import java.net.URL;

public record OrderLocation(String location) {
    public OrderLocation(URL url) {
        this(url.toString());
    }

    public URL url() {
        try {
            return new URL(this.location);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
