package com.asrevo.cvhome.commons.event;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultEventProcessor implements EventProcessor {

	private final List<EventImpl<Event>> eventsImpl;

	public DefaultEventProcessor(List<EventImpl<Event>> eventsImpl) {
		this.eventsImpl = eventsImpl;
	}

	@Override
	public void process(Event event) {
		List<EventImpl<Event>> processors = getProcessors(event);
		processors.forEach(it -> it.process(event));
		if (processors.isEmpty()) {
			List<String> currentProcessors = getAllProcessors().stream().map(EventImpl::type).toList();
			log.warn("could not find any event impl for {} in {}", event.eventType(), currentProcessors);
		}
	}

	@Override
	public List<EventImpl<Event>> getProcessors(Event event) {
		// @formatter:off
        return eventsImpl.stream()
                .filter(it -> it.type().equals(event.eventType()))
                .sorted(Comparator.comparing(EventImpl::order))
                .collect(Collectors.toList());
        // @formatter:on
	}

	@Override
	public List<EventImpl<Event>> getAllProcessors() {
		return eventsImpl;
	}

}
