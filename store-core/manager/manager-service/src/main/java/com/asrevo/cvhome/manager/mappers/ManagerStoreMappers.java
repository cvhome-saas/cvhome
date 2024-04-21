package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.commons.domain.Country;
import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.Phone;
import com.asrevo.cvhome.commons.dto.PodDto;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import com.asrevo.cvhome.router.commons.dto.ReferenceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface ManagerStoreMappers {
    ManagerStoreDto toDto(ManagerStoreEntity entity);

    @Mapping(target = "new", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "syncedInRouter", ignore = true)
    @Mapping(target = "syncedInStore", ignore = true)
    @Mapping(target = "isNew", ignore = true)
    ManagerStoreEntity toEntity(ListManagerStoreQuery managerStoreDto);

    default CreateManagerStoreRequest toCreateStoreRequest(Map<Object, Object> request) {
        String name = (String) request.get("name");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");
        Object address = request.get("address");
        String country = Optional.ofNullable(address)
                .map(it -> ((Map<String, String>) it))
                .map(it -> it.get("country"))
                .orElse(null);
        return new CreateManagerStoreRequest(name, new Phone(phone), Country.valueOf(country), new Email(email));
    }

    default PodReferenceDto toPodReferenceDto(CreateReferenceResponse it) {
        PodDto podDto = it.podDto();
        ReferenceDto referenceDto = it.referenceDto();
        return new PodReferenceDto(podDto.id(), podDto.region(), podDto.subRegion(), podDto.podType(), podDto.namespace(), podDto.location(), podDto.locationAlis(), referenceDto.reference().reference(), referenceDto.enabled());
    }

    default Map<Object, Object> toExternalCreateRequest(Map<Object, Object> request, String reference) {
        HashMap<Object, Object> newRequest = new HashMap<>(request);
        newRequest.put("code", reference);
        return newRequest;
    }

    default PageImpl<Object> toPage(List<Object> it, Page<ManagerStoreDto> internalStores) {
        return new PageImpl<>(it, internalStores.getPageable(), internalStores.getTotalElements());
    }

}
