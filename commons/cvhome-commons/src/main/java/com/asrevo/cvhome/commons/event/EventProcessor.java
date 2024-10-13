package com.asrevo.cvhome.commons.event;

import java.util.List;

public interface EventProcessor {
    void process(Event event);

    List<EventImpl<Event>> getProcessors(Event event);

    List<EventImpl<Event>> getProcessors(Class<? extends Event> tClass);

    List<EventImpl<Event>> getAllProcessors();
}
