package com.asrevo.cvhome.catalog;

import org.springframework.boot.SpringApplication;

public final class TestCatalogApplication {
    private TestCatalogApplication() {
    }

    static void main(String[] args) {
        SpringApplication.from(CatalogApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
