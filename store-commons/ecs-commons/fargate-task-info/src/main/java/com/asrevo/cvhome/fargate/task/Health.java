package com.asrevo.cvhome.fargate.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Health {

    private String status;

    private String statusSince;

}
