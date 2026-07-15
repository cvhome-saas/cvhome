package com.asrevo.cvhome.cua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CuaApplication {

    private CuaApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(CuaApplication.class, args);
    }

}
