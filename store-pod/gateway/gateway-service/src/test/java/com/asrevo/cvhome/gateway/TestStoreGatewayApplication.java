package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestStoreGatewayApplication {
  public static void main(String[] args) {
    SpringApplication.from(StoreGatewayApplication::main).with(TestStoreGatewayApplication.class).run(args);
  }

}
