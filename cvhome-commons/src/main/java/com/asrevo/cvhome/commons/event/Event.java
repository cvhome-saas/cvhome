package com.asrevo.cvhome.commons.event;

import com.asrevo.cvhome.commons.domain.Identifier;

import java.util.Map;

public interface Event<T extends Identifier> {
    void setId(T id);

    String eventType();

    Map<String, String> data();
}

