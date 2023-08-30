package com.asrevo.cvhome.commons.event;

import java.util.Map;

public record SimpleEvent(String eventType, Map<String, String> data) implements Event {
}
