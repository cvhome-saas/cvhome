package com.asrevo.cvhome.tenancy.manager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;

public interface InternalOrgService {

    ManagerOrgId createOrgForUser(Email email);

    Page<ManagerOrgDto> findAll(Pageable pageable);

    ManagerOrgDto findOne(ManagerOrgId id);

}
