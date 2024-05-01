package com.asrevo.cvhome.router.commons.dto;

import com.asrevo.cvhome.commons.domain.PodRegion;
import com.asrevo.cvhome.commons.domain.PodSubRegion;
import com.asrevo.cvhome.commons.domain.PodType;

public record CreatePodDto(PodRegion region, PodSubRegion subRegion, PodType podType, String namespace, String location,
                           String locationAlis) {

}
