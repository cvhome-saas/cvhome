package com.asrevo.cvhome.content;

import org.springframework.boot.SpringApplication;

public final class TestContentApplication {
    private TestContentApplication() {

    }

    static void main(String[] args) {
        SpringApplication.from(ContentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
