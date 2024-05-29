package com.asrevo.cvhome.store.core.model.system;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class IntegrationModuleConfiguration extends IntegrationModuleEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean defaultSelected;
    private Map<String, String> integrationKeys = new HashMap<>();
    private Map<String, List<String>> integrationOptions = new HashMap<>();
    private List<String> requiredKeys = new ArrayList<>();
    private String configurable = null;


}
