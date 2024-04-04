package com.asrevo.cvhome.store.core.model.system;

import lombok.Getter;
import lombok.Setter;

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
    private static final long serialVersionUID = 1L;
    private boolean defaultSelected;
    private Map<String, String> integrationKeys = new HashMap<String, String>();
    private Map<String, List<String>> integrationOptions = new HashMap<String, List<String>>();
    private List<String> requiredKeys = new ArrayList<String>();
    private String configurable = null;


}
