package com.asrevo.cvhome.order;

import org.springframework.boot.SpringApplication;

public class TestOrderApplication {

    public static void main(String[] args) {
        SpringApplication.from(OrderApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
