package com.asrevo.cvhome.domaincertificatemanager.mappers;

import com.asrevo.cvhome.domaincertificatemanager.commons.dto.PodDto;
import com.asrevo.cvhome.domaincertificatemanager.entity.PodEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PodMappers {
  PodDto toPodDto(PodEntity entity);

}
