package com.asrevo.cvhome.store.controller;

import com.asrevo.cvhome.store.config.SecurityConfig;
import com.asrevo.cvhome.store.controller.admin.AdminProductsController;
import com.asrevo.cvhome.store.service.AdminProductService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(AdminProductsController.class)
@Import(SecurityConfig.class)
@Testcontainers
@Tag("integration-test")
class AdminProductsControllerTest {

    @Autowired
    WebTestClient client;
    @MockBean
    AdminProductService productService;


    @Test
    void createProduct() {
    }

    @Test
    void updateProduct() {
    }

    @Test
    void findAll() {
/*        StoreId storeId = StoreId.newId();
        Mockito.when(productService.findAll(any(StoreId.class), any(Pageable.class))).thenReturn(List.of());
        client.mutateWith(mockJwt().jwt((jwt) -> jwt.subject("test-subject")))
                .get().uri("/api/v1/products/find-all?storeId=" + storeId.id().toString()).exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(it -> {
                    String responseBody = it.getResponseBody();
                    System.out.println(responseBody);
                });*/
    }
}
