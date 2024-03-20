package com.asrevo.cvhome.router.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.router.commons.domain.PodId;
import com.asrevo.cvhome.router.commons.domain.PodRegion;
import com.asrevo.cvhome.router.commons.domain.PodSubRegion;
import com.asrevo.cvhome.router.commons.domain.PodType;
import com.asrevo.cvhome.router.commons.dto.CreatePodDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("pod")
public class PodEntity extends BaseEntity<PodEntity, PodId> {
    private PodRegion region;
    private PodSubRegion subRegion;
    private PodType podType;
    private String namespace;
    private String location;
    private String locationAlis;

    public static PodEntity create(CreatePodDto dto) {
        PodEntity entity = new PodEntity();
        entity.setNew();
        entity.setRegion(dto.region());
        entity.setSubRegion(dto.subRegion());
        entity.setPodType(dto.podType());
        entity.setNamespace(dto.namespace());
        entity.setLocation(dto.location());
        entity.setLocationAlis(dto.locationAlis());
        return entity;
    }

    @Override
    protected PodId generateId() {
        return PodId.newId();
    }
}

