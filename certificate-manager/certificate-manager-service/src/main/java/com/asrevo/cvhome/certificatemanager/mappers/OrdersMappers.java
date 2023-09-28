package com.asrevo.cvhome.certificatemanager.mappers;

import com.asrevo.cvhome.certificatemanager.commons.dto.OrdersCreateResponseDto;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrdersMappers {
    OrdersCreateResponseDto toOrdersCreateResponse(OrdersEntity entity);
}
