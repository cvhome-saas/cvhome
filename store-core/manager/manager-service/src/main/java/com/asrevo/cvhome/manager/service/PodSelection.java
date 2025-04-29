package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.OrgId;
import com.asrevo.cvhome.commons.domain.PodId;

public interface PodSelection {
    PodId next(OrgId orgId, PodId prefaredPodId);
}
