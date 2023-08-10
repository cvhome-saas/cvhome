package com.asrevo.cvhome.certificatemanager.domain;

import java.util.Map;

public record Challenges(Map<String, Map<String, String>> challenges) {
}
