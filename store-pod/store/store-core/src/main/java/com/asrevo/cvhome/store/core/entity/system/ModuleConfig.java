package com.asrevo.cvhome.store.core.entity.system;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ModuleConfig {

    private String scheme;
    private String host;
    private String port;
    private String uri;
    private String env;
    private String config1;
    private String config2;
}
