package com.asrevo.cvhome.catalog;

import org.springframework.boot.SpringApplication;

public class TestCatalogApplication {
    private TestCatalogApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(CatalogApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
