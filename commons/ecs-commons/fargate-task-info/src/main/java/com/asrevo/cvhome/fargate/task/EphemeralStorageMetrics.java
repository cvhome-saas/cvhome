package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EphemeralStorageMetrics {
    @JsonProperty("Utilized")
    private int utilized;

    @JsonProperty("Reserved")
    private int reserved;
}
