package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ImageId;
import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("image")
public class ImageEntity extends BaseEntity<ImageEntity, ImageId> {
    private String name;
    @Column("link")
    @Embedded(onEmpty = USE_NULL)
    private ImageLink imageLink;
    private StoreId storeId;

    public static ImageEntity create(StoreId storeId,String name, ImageLink link) {
        ImageEntity image = new ImageEntity();
        image.setNew();
        image.setStoreId(storeId);
        image.setImageLink(link);
        image.setName(name);
        return image;
    }

    @Override
    protected ImageId generateId() {
        return ImageId.newId();
    }
}
