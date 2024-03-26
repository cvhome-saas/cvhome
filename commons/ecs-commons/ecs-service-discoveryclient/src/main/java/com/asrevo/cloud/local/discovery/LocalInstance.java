package com.asrevo.cloud.local.discovery;

import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ashraf
 * model contain instance inforatmion
 */
@Getter
@Setter
public class LocalInstance {
    private URL url;
    private Map<String, String> metadata = new HashMap<>();
}
