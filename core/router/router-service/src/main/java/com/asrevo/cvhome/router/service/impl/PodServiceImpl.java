package com.asrevo.cvhome.router.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;

import com.asrevo.cvhome.router.commons.domain.PodId;
import com.asrevo.cvhome.router.commons.dto.PodDto;
import com.asrevo.cvhome.router.mappers.PodMappers;
import com.asrevo.cvhome.router.repository.PodRepository;
import com.asrevo.cvhome.router.service.PodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PodServiceImpl implements PodService {
    private final PodRepository podRepository;
    private final PodMappers podMappers;


    @Override
    public PodDto getPod(PodId podId) {
        return podRepository.findById(podId).map(podMappers::toPodDto).orElse(null);
    }
}
