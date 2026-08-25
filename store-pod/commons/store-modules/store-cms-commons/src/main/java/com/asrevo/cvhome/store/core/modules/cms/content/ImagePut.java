package com.asrevo.cvhome.store.core.modules.cms.content;

import java.util.List;

import com.asrevo.cvhome.store.core.entity.content.InputContentFile;

public interface ImagePut {

    void addImage(String merchantStoreCode, InputContentFile image);

    void addImages(String merchantStoreCode, List<InputContentFile> imagesList);

}
