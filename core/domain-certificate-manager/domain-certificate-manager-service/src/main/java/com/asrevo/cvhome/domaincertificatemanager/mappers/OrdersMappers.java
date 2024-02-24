package com.asrevo.cvhome.domaincertificatemanager.mappers;

import com.asrevo.cvhome.domaincertificatemanager.commons.dto.OrdersCreateResponseDto;
import com.asrevo.cvhome.domaincertificatemanager.entity.OrdersEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrdersMappers {
    OrdersCreateResponseDto toOrdersCreateResponse(OrdersEntity entity);
}
