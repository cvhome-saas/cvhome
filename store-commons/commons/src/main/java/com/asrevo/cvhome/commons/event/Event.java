package com.asrevo.cvhome.commons.event;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Event {

    String eventType();

    Map<String, String> data();

    @JsonIgnore
    default List<String> getDestinations() {
        return List.of();
    }

}
