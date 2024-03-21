package com.asrevo.cvhome.router.service.impl;

import com.asrevo.cvhome.commons.domain.Country;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.commons.dto.PodDto;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.router.commons.dto.AddAlisDto;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreatePodDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import com.asrevo.cvhome.router.entity.PodEntity;
import com.asrevo.cvhome.router.entity.ReferenceAlisEntity;
import com.asrevo.cvhome.router.entity.ReferenceEntity;
import com.asrevo.cvhome.router.mappers.PodMappers;
import com.asrevo.cvhome.router.mappers.ReferenceMappers;
import com.asrevo.cvhome.router.repository.PodRepository;
import com.asrevo.cvhome.router.repository.ReferenceRepository;
import com.asrevo.cvhome.router.service.PodService;
import com.asrevo.cvhome.router.utils.ErrorCodes;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PodServiceImpl implements PodService {
    private final ReferenceRepository referenceRepository;
    private final PodRepository podRepository;
    private final PodMappers podMappers;
    private final ReferenceMappers referenceMappers;

    @Transactional
    @Override
    public PodDto create(CreatePodDto dto) {
        PodEntity entity = PodEntity.create(dto);
        return podMappers.toDto(podRepository.save(entity));
    }

    @Override
    public Page<PodEntity> findAll(PodDto podDto, Pageable pageable) {
        PodEntity entity = podMappers.toEntity(podDto);
        return podRepository.findAll(Example.of(entity), pageable);
    }

    @Override
    public PodDto selectPod(Domain domain, Country country) {
        PodEntity podEntity = podRepository.findAll().stream().findFirst().orElseThrow(() -> new OperationExecution(ErrorCodes.pod_not_exist));
        return podMappers.toDto(podEntity);
    }

    @Transactional
    @Override
    public CreateReferenceResponse createReference(CreateNewReferenceDto createReferenceDto) {
        PodDto podDto = selectPod(createReferenceDto.domain(), createReferenceDto.country());
        ReferenceEntity entity = ReferenceEntity.create(createReferenceDto.reference(), podDto.id());
        referenceRepository.save(entity);
        entity.addAlis(ReferenceAlisEntity.create(createReferenceDto.domain()));
        ReferenceEntity saved = referenceRepository.save(entity);
        return new CreateReferenceResponse(podDto, referenceMappers.toDto(saved));
    }

    @Transactional
    @Override
    public CreateReferenceResponse addAlis(AddAlisDto addAlisDto) {
        ReferenceEntity entity = getReferenceEntity(addAlisDto.reference());

        entity.addAlis(ReferenceAlisEntity.create(addAlisDto.domain()));
        ReferenceEntity saved = referenceRepository.save(entity);
        PodEntity pod = podRepository.findById(saved.getPod().getId())
                .orElseThrow(() -> new OperationExecution(ErrorCodes.pod_not_exist));
        return new CreateReferenceResponse(podMappers.toDto(pod), referenceMappers.toDto(saved));
    }


    @Override
    public PodReferenceDto getAllocation(Domain domain) {
        return referenceRepository.getReferenceAllocationByDomain(domain.domain())
                .orElseThrow(() -> new OperationExecution(ErrorCodes.alis_not_exist));
    }

    @Override
    public PodReferenceDto getAllocation(DomainReference reference) {
        return referenceRepository.getReferenceAllocationByReference(reference.reference())
                .orElseThrow(() -> new OperationExecution(ErrorCodes.alis_not_exist));
    }

    @Transactional
    @Override
    public Boolean enableReference(DomainReference reference) {
        ReferenceEntity entity = getReferenceEntity(reference);
        referenceRepository.save(entity.enableReference());
        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public Boolean disableReference(DomainReference reference) {
        ReferenceEntity entity = getReferenceEntity(reference);
        referenceRepository.save(entity.disableReference());
        return Boolean.TRUE;
    }

    private ReferenceEntity getReferenceEntity(DomainReference reference) {
        return referenceRepository.findByReference(reference)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.reference_not_exist));
    }

}
