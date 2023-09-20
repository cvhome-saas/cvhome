package com.asrevo.cvhome.commons.event.domain;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.commons.event.Event;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public abstract class DomainEvent implements Event<DomainId> {
    private DomainId id;
    private Domain domain;

    @Override
    public void setId(DomainId id) {
        this.id = id;
    }

    @Override
    public Map<String, String> data() {
        return Map.of();
    }

}
