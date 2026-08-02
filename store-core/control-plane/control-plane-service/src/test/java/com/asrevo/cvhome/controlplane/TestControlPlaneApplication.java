package com.asrevo.cvhome.controlplane;

import org.springframework.boot.SpringApplication;

public final class TestControlPlaneApplication {

    private TestControlPlaneApplication() {

    }

    static void main(String[] args) {
        SpringApplication.from(ControlPlaneApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
