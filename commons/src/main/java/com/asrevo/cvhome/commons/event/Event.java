package com.asrevo.cvhome.commons.event;

import java.util.Map;

public interface Event {
    String eventType();

    Map<String, String> data();
}

