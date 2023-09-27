package com.asrevo.cvhome.commons.event;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultEventProcessor implements EventProcessor {
    private final List<EventImpl<Event>> eventsImpl;

    public DefaultEventProcessor(List<EventImpl<Event>> eventsImpl) {
        this.eventsImpl = eventsImpl;
    }

    @Override
    public void process(Event event) {
        getProcessors(event.getClass()).forEach(it -> it.process(event));
    }

    @Override
    public List<EventImpl<Event>> getProcessors(Event event) {
        if (event == null) {
            return List.of();
        }
        return getProcessors(event.getClass());
    }

    @Override
    public List<EventImpl<Event>> getProcessors(Class<? extends Event> tClass) {
        // @formatter:off
        return eventsImpl.stream()
                .filter(it -> it.type().equals(tClass.getName()))
                .sorted(Comparator.comparing(EventImpl::order))
                .collect(Collectors.toList());
        // @formatter:on
    }


    @Override
    public List<EventImpl<Event>> getAllProcessors() {
        return eventsImpl;
    }

}
