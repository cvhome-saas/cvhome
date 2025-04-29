package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;

public record SliderImage(Integer priority, String url)
        implements Serializable, Comparable<SliderImage> {

    @Override
    public int compareTo(SliderImage o) {
        return o.url.compareTo(this.url);
    }
}
