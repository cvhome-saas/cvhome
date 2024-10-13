package com.asrevo.cvhome.store.core.model.system;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IntegrationModuleSummaryEntity extends IntegrationModuleEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private boolean configured;
    private String image;
    private String binaryImage;
    private List<String> requiredKeys = new ArrayList<>();
    private String configurable = null;
}
