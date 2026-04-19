package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;

public record SocialLink(String provider, String url) implements Serializable, Comparable<SocialLink> {
    public SocialLink {
        SocialProvider.valueOf(provider);
    }

    @Override
    public int compareTo(SocialLink o) {
        return o.provider.compareTo(this.provider);
    }
}
