package com.asrevo.cvhome.router.service;

import com.asrevo.cvhome.router.commons.domain.PodId;
import com.asrevo.cvhome.router.commons.dto.PodDto;

public interface PodService {

    PodDto getPod(PodId podId);
}
