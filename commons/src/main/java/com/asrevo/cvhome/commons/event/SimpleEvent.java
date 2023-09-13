package com.asrevo.cvhome.commons.event;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SimpleEvent implements Event {
    private String eventType;
    private Map<String, String> data;

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public Map<String, String> data() {
        return data;
    }
}
