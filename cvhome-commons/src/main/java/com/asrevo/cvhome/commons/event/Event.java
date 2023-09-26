package com.asrevo.cvhome.commons.event;

import com.asrevo.cvhome.commons.domain.Identifier;

import java.util.List;
import java.util.Map;

public interface Event<T extends Identifier> {
    void setId(T id);

    String eventType();

    Map<String, String> data();

    default List<String> getDestinations() {
        return List.of();
    }
}

