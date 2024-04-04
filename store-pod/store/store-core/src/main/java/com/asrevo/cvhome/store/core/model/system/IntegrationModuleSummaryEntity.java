package com.asrevo.cvhome.store.core.model.system;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class IntegrationModuleSummaryEntity extends IntegrationModuleEntity {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean configured;
    private String image;
    private String binaryImage;
    private List<String> requiredKeys = new ArrayList<String>();
    private String configurable = null;

}
