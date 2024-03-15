package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.dto.ImageDto;
import com.asrevo.cvhome.product.mappers.ImageMapperImpl;
import com.asrevo.cvhome.product.service.impl.ImageServiceImpl;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static rocks.cleancode.hamcrest.record.HasFieldMatcher.field;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JacksonAutoConfiguration.class, ImageServiceImpl.class, ImageMapperImpl.class})
@Testcontainers
@Tag("integration-test")
class ImageServiceIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private ImageServiceImpl imageService;

    @Test
    void create() {
        StoreId storeId = StoreId.newId();
        ImageDto iphone = new ImageDto("iphone ", new ImageLink("https://google.com"));
        ImageDto imageDto = imageService.create(storeId, iphone);
        assertThat(imageDto, notNullValue());
        assertThat(imageDto.name(), is(iphone.name()));
        assertThat(imageDto.imageLink(), is(iphone.imageLink()));
    }

    @Test
    void findImages() {
        StoreId storeId = StoreId.newId();
        ImageDto iphone = new ImageDto("iphone", new ImageLink("https://google.com"));
        ImageDto imageDto = imageService.create(storeId, iphone);
        List<ImageDto> images = imageService.findImages(storeId, null, PageRequest.of(0, 10));
        assertThat(images, hasSize(greaterThan(0)));
    }

    @Test
    void findImagesWithName() {
        StoreId storeId = StoreId.newId();
        ImageDto iphone = new ImageDto("iphone", new ImageLink("https://google.com"));
        imageService.create(storeId, iphone);
        List<ImageDto> images = imageService.findImages(storeId, "PHO", PageRequest.of(0, 10));
        assertThat(images, hasSize(greaterThan(0)));
        assertThat(images, everyItem(field("name", is(equalTo("iphone")))));
    }
}