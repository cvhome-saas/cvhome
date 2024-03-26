package com.asrevo.cvhome.dcm.mappers;

import com.asrevo.cvhome.dcm.commons.dto.OrdersCreateResponseDto;
import com.asrevo.cvhome.dcm.entity.OrdersEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrdersMappers {
    OrdersCreateResponseDto toOrdersCreateResponse(OrdersEntity entity);
}
