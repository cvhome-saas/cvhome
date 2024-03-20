package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.store.commons.dto.ImageDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminImageService {
    ImageDto create(StoreId storeId, ImageDto imageDto);

    List<ImageDto> findImages(StoreId storeId, String name, Pageable pageable);
}
