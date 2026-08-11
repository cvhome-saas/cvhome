package com.asrevo.cvhome.tenancy.manager.service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;

public interface PodSelection {

    PodId next(ManagerOrgId orgId, PodId prefaredPodId);

}
