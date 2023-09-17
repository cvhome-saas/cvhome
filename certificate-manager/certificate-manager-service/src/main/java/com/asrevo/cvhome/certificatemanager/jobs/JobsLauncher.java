package com.asrevo.cvhome.certificatemanager.jobs;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

import static org.jobrunr.scheduling.cron.Cron.every5minutes;

@Component
@AllArgsConstructor
public class JobsLauncher {

    private final JobScheduler jobScheduler;

    public static String every1minutes() {
        return "*/1 * * * *";
    }

    @PostConstruct
    public void scheduleRecurrently() {
        jobScheduler.scheduleRecurrently(every5minutes(), AcmJobs::enqueuePreValidationInvalid);
        jobScheduler.scheduleRecurrently(every1minutes(), AcmJobs::enqueueFailedOrder);
    }
}
