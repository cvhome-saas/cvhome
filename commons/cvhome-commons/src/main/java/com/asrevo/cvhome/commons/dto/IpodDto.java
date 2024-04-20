package com.asrevo.cvhome.commons.dto;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.PodRegion;
import com.asrevo.cvhome.commons.domain.PodSubRegion;
import com.asrevo.cvhome.commons.domain.PodType;

public interface IpodDto {
    PodId id();

    PodRegion region();

    PodSubRegion subRegion();

    PodType podType();

    String namespace();

    String location();

    String locationAlis();
}
