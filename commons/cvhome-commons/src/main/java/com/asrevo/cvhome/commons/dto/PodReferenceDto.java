package com.asrevo.cvhome.commons.dto;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.PodRegion;
import com.asrevo.cvhome.commons.domain.PodSubRegion;
import com.asrevo.cvhome.commons.domain.PodType;

public record PodReferenceDto(PodId id, PodRegion region, PodSubRegion subRegion, PodType podType, String namespace,
                              String location, String locationAlis, String reference,
                              Boolean enabled) implements IpodDto {


    public static PodReferenceDto from(String location, String reference) {
        return new PodReferenceDto(PodId.newId(), PodRegion.LOCAL, PodSubRegion.R1, PodType.EXTERNAL, null, location, null, reference, Boolean.TRUE);
    }
}
