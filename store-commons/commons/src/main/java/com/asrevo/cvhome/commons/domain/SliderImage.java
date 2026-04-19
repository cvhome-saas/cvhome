package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;

public record SliderImage(Integer priority, String name) implements Serializable, Comparable<SliderImage> {

    @Override
    public int compareTo(SliderImage o) {
        return o.name.compareTo(this.name);
    }
}
