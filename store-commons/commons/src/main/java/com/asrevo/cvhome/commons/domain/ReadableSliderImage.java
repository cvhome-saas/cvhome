package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;

public record ReadableSliderImage(Integer priority, String name, String url)
        implements Serializable {}
