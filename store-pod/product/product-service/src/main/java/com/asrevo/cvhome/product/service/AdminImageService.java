package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.dto.ImageDto;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminImageService {
    ImageDto create(StoreId storeId, ImageDto imageDto);

    List<ImageDto> findImages(StoreId storeId,String name, Pageable pageable);
}
