package com.asrevo.cvhome.podregistry;

import org.springframework.boot.SpringApplication;

public final class TestPodRegistryApplication {

    private TestPodRegistryApplication() {

    }

    static void main(String[] args) {
        SpringApplication.from(PodRegistryApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
