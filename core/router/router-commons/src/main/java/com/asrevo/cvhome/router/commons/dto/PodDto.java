package com.asrevo.cvhome.router.commons.dto;


import com.asrevo.cvhome.router.commons.domain.PodId;
import com.asrevo.cvhome.router.commons.domain.PodRegion;
import com.asrevo.cvhome.router.commons.domain.PodSubRegion;
import com.asrevo.cvhome.router.commons.domain.PodType;

public record PodDto(PodId id, PodRegion region, PodSubRegion subRegion, PodType podType, String namespace,
                     String location, String locationAlis) {
}
