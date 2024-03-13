package com.asrevo.cvhome.product.controller;

import com.asrevo.cvhome.product.config.SecurityConfig;
import com.asrevo.cvhome.product.service.ProductService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(AdminProductsController.class)
@Import(SecurityConfig.class)
@Testcontainers
@Tag("integration-test")
class AdminProductsControllerTest {

  @Autowired
  WebTestClient client;
  @MockBean
  ProductService productService;



  @Test
  void createProduct() {
  }

  @Test
  void updateProduct() {
  }

  @Test
  void findAll() {
    StoreId storeId = StoreId.newId();
    Mockito.when(productService.findAll(any(StoreId.class), any(Pageable.class))).thenReturn(List.of());
    client.mutateWith(mockJwt().jwt((jwt) -> jwt.subject("test-subject")))
      .get().uri("/api/v1/products/find-all?storeId="+ storeId.id().toString()).exchange()
      .expectStatus().isOk()
      .expectBody(String.class)
      .consumeWith(it -> {
        String responseBody = it.getResponseBody();
        System.out.println(responseBody);
      });
  }
}
