package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.event.Event;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public abstract class OrderEvent implements Event {
    private Long id;

    @Override
    public Map<String, String> data() {
        return Map.of();
    }

}
