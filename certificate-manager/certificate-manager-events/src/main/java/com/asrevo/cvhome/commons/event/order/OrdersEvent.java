package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.event.Event;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public abstract class OrdersEvent implements Event<OrdersId> {
    private OrdersId id;

    @Override
    public Map<String, String> data() {
        return Map.of();
    }

    @Override
    public List<String> getDestinations() {
        return List.of("outOrderEvents-out-0");
    }
}
