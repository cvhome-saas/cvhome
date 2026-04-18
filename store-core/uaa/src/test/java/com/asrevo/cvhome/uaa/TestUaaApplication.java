package com.asrevo.cvhome.uaa;

import org.springframework.boot.SpringApplication;

public class TestUaaApplication {

    public static void main(String[] args) {
        SpringApplication.from(UaaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
