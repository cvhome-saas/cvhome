package com.asrevo.cvhome.commons.event;


public interface EventImpl<T extends Event> {

    void process(T event);
}
