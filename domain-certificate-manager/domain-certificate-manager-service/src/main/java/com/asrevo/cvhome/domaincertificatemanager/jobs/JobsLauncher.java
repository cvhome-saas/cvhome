package com.asrevo.cvhome.domaincertificatemanager.jobs;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
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
