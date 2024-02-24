package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Limits {
    @JsonProperty("CPU")
    private double cpu;
    @JsonProperty("Memory")
    private int memory;
}
