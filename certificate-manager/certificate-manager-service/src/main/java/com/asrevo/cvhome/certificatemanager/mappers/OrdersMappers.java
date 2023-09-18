package com.asrevo.cvhome.certificatemanager.mappers;

import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import com.asrevo.cvhome.commons.dto.OrdersCreateResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrdersMappers {
    OrdersCreateResponseDto toOrdersCreateResponse(OrdersEntity entity);
}
