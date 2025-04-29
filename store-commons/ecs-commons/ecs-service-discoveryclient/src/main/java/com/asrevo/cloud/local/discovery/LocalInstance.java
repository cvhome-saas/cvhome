package com.asrevo.cloud.local.discovery;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

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
