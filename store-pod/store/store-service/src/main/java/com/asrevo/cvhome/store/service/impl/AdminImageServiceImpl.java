package com.asrevo.cvhome.store.service.impl;

import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.store.commons.dto.ImageDto;
import com.asrevo.cvhome.store.entity.ImageEntity;
import com.asrevo.cvhome.store.mappers.ImageMapper;
import com.asrevo.cvhome.store.repository.ImageRepository;
import com.asrevo.cvhome.store.service.AdminImageService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminImageServiceImpl implements AdminImageService {
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;


    @Override
    public ImageDto create(StoreId storeId, ImageDto imageDto) {
        ImageEntity saved = imageRepository.save(ImageEntity.create(storeId, imageDto.name(), imageDto.imageLink()));
        return imageMapper.toDto(saved);
    }

    @Override
    public List<ImageDto> findImages(StoreId storeId, String name, Pageable pageable) {
        if (StringUtils.hasText(name)) {
            return imageRepository.findByStoreIdAndNameIsContainingIgnoreCase(storeId, name, pageable)
                    .stream()
                    .map(imageMapper::toDto)
                    .toList();
        } else {
            return imageRepository.findByStoreId(storeId, pageable)
                    .stream()
                    .map(imageMapper::toDto)
                    .toList();
        }
    }
}
