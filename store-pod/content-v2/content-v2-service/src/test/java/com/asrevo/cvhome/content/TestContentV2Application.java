package com.asrevo.cvhome.content;

import org.springframework.boot.SpringApplication;

public final class TestContentV2Application {
    private TestContentV2Application() {

    }

    static void main(String[] args) {
        SpringApplication.from(ContentV2Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
