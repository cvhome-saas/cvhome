package com.asrevo.cvhome.cua;

import org.springframework.boot.SpringApplication;

public class TestCuaApplication {

    private TestCuaApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(CuaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
