package com.asrevo.cvhome.domaincertificatemanager.repository;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodId;
import com.asrevo.cvhome.domaincertificatemanager.entity.PodEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface PodRepository extends ListCrudRepository<PodEntity, PodId> {
}
