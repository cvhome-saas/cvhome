package org.revo.streamer.codec.sdp;

import java.util.Map;

public record ElementSpecific(String encodingName, int clockRate, Map<String, String> attr) {
}
