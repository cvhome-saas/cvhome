package com.asrevo.cvhome.store.core.modules.cms.content;

import java.util.List;

import com.asrevo.cvhome.store.core.entity.content.InputContentFile;

public interface ImagePut {

    void addImage(final String merchantStoreCode, InputContentFile image);

    void addImages(final String merchantStoreCode, List<InputContentFile> imagesList);

}
