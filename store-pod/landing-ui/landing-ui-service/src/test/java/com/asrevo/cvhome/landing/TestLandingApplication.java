package com.asrevo.cvhome.landing;

import com.asrevo.cvhome.landing.service.CategoryService;
import com.asrevo.cvhome.landing.service.ProductService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestLandingApplication {

    @Bean
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:13"));
    }

    public static void main(String[] args) {
        SpringApplication.from(LandingApplication::main).with(TestLandingApplication.class).run(args);
    }

    @Bean
    public CommandLineRunner runner(ProductService productService, CategoryService categoryService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                categoryService.getStoreCategoriesView(StoreId.newId());
                productService.findAll(StoreId.newId(), PageRequest.of(2, 9));
//                productService.findAll(Utils.serialize(StoreId.newId()), PageRequest.of(0, 10));
            }
        };
    }
}
