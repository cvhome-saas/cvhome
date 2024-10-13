package com.asrevo.cvhome.commons.event;

import org.springframework.scheduling.annotation.Async;

public interface EventImpl<T extends Event> {
    @Async
    void process(T event);

    String type();

    default Integer order() {
        return 0;
    }
}
