package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodRegion;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodSubRegion;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodType;

public record PodDto(PodId id, PodRegion region, PodSubRegion subRegion, PodType podType, String namespace,
                     String location, String locationAlis) {
}
