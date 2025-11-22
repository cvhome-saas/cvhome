package com.asrevo.cvhome.controlplane.subscription.controller;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventProcessor;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/http-events")
@Slf4j
@AllArgsConstructor
public class HttpEventsController {

	private final EventProcessor eventProcessor;

	private final JsonMapper mapper = JsonMapper.builder()
		.addModules(new JavaTimeModule(), new Jdk8Module())
		.setDefaultTyping(new StdTypeResolverBuilder().init(JsonTypeInfo.Id.CLASS, null)
			.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT))
		.build();

	@PostMapping(value = "on", consumes = "application/json")
	public void on(@RequestBody String rawEvent) throws JsonProcessingException {
		Event event = mapper.readValue(rawEvent, Event.class);
		eventProcessor.process(event);
	}

}
