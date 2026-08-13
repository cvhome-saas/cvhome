package com.asrevo.cvhome.content.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.service.ContentV2Service;

@Component
public class ContentLifecycleScheduler {
    private final ContentV2Service contentService;

    public ContentLifecycleScheduler(ContentV2Service contentService) {
        this.contentService = contentService;
    }

    @Scheduled(fixedDelayString = "${content.lifecycle.scheduler-delay:PT5S}")
    public void processDueContent() throws ContentNotFoundException, ContentVersionConflictException,
            IllegalContentTransitionException {
        contentService.processDueContent();
    }
}
