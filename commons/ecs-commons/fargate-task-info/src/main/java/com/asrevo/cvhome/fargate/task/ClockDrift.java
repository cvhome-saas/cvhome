package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ClockDrift {
    @JsonProperty("ClockErrorBound")
    private double clockErrorBound;
    @JsonProperty("ReferenceTimestamp")
    private Date referenceTimestamp;
    @JsonProperty("ClockSynchronizationStatus")
    private String clockSynchronizationStatus;
}
