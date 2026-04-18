package com.asrevo.cvhome.controlplane;

import org.springframework.boot.SpringApplication;

public class TestControlPlaneApplication {

    public static void main(String[] args) {
        SpringApplication.from(ControlPlaneApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
