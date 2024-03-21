package com.asrevo.cvhome.commons.dto;

import com.asrevo.cvhome.commons.domain.*;

public record PodReferenceDto(PodId id, PodRegion region, PodSubRegion subRegion, PodType podType, String namespace,
                              String location, String locationAlis, String reference,Boolean enabled) {
}
