package com.asrevo.cvhome.content;

import org.springframework.boot.SpringApplication;

public class TestContentApplication {

    public static void main(String[] args) {
        SpringApplication.from(ContentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
