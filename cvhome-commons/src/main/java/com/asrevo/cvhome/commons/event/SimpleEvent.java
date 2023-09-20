package com.asrevo.cvhome.commons.event;

import com.asrevo.cvhome.commons.domain.Identifier;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SimpleEvent<T extends Identifier> implements Event<T> {
    private T id;
    private String eventType;
    private Map<String, String> data;

    @Override
    public void setId(T id) {
        this.id = id;
    }

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public Map<String, String> data() {
        return data;
    }
}
