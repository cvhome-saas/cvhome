package com.asrevo.cvhome.controlplane.manager.service;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerOrgDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternalOrgService {

	ManagerOrgId createOrgForUser(Email email);

	Page<ManagerOrgDto> findAll(Pageable pageable);

	ManagerOrgDto findOne(ManagerOrgId id);

}
