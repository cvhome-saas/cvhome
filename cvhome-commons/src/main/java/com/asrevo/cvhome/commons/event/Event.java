package com.asrevo.cvhome.commons.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

public interface Event {
    String eventType();

    Map<String, String> data();

    @JsonIgnore
    default List<String> getDestinations() {
        return List.of();
    }
}

