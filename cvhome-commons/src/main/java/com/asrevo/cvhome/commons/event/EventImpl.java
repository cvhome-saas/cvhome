package com.asrevo.cvhome.commons.event;

public interface EventImpl<T extends Event> {
    void process(T event);

    String type();

    default Integer order() {
        return 0;
    }
}


