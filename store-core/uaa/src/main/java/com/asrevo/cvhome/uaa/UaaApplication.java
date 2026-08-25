package com.asrevo.cvhome.uaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class UaaApplication {

    private UaaApplication() {

    }

    public static void main(String[] args) {
        SpringApplication.run(UaaApplication.class, args);
    }

}
