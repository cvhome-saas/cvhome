package com.asrevo.cvhome.router.controller;

import com.asrevo.cvhome.router.commons.dto.CreatePodDto;
import com.asrevo.cvhome.router.commons.dto.PodDto;
import com.asrevo.cvhome.router.entity.PodEntity;
import com.asrevo.cvhome.router.service.PodService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin/pod")
@AllArgsConstructor
@Slf4j
public class AdminPodController {
    private final PodService podService;

    @PostMapping("create")
    public PodDto create(@RequestBody CreatePodDto podDto) {
        return podService.create(podDto);
    }

    @PostMapping("find-all")
    public Page<PodEntity> findAll(@RequestBody PodDto podDto, Pageable pageable) {
        return podService.findAll(podDto, pageable);
    }

}
