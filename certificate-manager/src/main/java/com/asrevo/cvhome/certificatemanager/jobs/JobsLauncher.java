package com.asrevo.cvhome.certificatemanager.jobs;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JobsLauncher {

    private final JobScheduler jobScheduler;

    @PostConstruct
    public void scheduleRecurrently() {
        jobScheduler.scheduleRecurrently("*/1 * * * *", AcmJobs::run);
    }
}
