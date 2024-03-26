package com.asrevo.cvhome.dcm.jobs;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "org.jobrunr.job-scheduler", name = "enabled")
public class JobsLauncher {

    private final JobScheduler jobScheduler;

    public static String every1minutes() {
        return "*/1 * * * *";
    }

    @PostConstruct
    public void scheduleRecurrently() {
        jobScheduler.scheduleRecurrently(every1minutes(), AcmJobs::orderSystemDomains);
    }
}
