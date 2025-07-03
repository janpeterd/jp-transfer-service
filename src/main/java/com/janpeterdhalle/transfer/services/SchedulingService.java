package com.janpeterdhalle.transfer.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulingService {
    private final SharedLinkRepository sharedLinkRepository;
    private final TaskScheduler taskScheduler;
    private final SharedLinkService sharedLinkService;

    @PostConstruct
    public void scheduleExistingEvents() {
        // Schedule all upcoming events that haven't been archived.
        List<SharedLink> sharedLinks = sharedLinkRepository.findAll();
        for (SharedLink sharedLink : sharedLinks) {
            scheduleExpiry(sharedLink);
        }
    }

    public void scheduleExpiry(SharedLink sharedLink) {
        if (sharedLink.getExpiresAt() == null) {
            log.warn("Cannot schedule ride assignment for event {} as it has no registration deadline",
                    sharedLink.getId());
            return;
        }

        // Convert the event's registration deadline to a cron expression.
        String cronExpression = getCronExpression(sharedLink.getExpiresAt());
        Trigger trigger = new CronTrigger(cronExpression);

        // Schedule the task using the TaskScheduler.
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            try {
                log.info("Link Expired, deleting data for link {}", sharedLink.getId());
                sharedLinkService.deletedAssociatedData(sharedLink);
                log.info("Deleted data for link {} now deleting link", sharedLink.getId());
                sharedLinkRepository.delete(sharedLink);
            } catch (Exception e) {
                log.error("Error deleting data or link {}", sharedLink.getId(), e);
            }
        }, trigger);

        log.info("Scheduled link deletion {} at {}. Future: {}",
                sharedLink.getId(), sharedLink.getExpiresAt(), future);
    }

    /**
     * Converts a LocalDateTime to a cron expression.
     * Spring cron expressions have the format: "sec min hour day month
     * day-of-week".
     * Here we use '?' for the day-of-week.
     */
    private String getCronExpression(LocalDateTime dateTime) {
        return String.format("%d %d %d %d %d ?",
                dateTime.getSecond(),
                dateTime.getMinute(),
                dateTime.getHour(),
                dateTime.getDayOfMonth(),
                dateTime.getMonthValue());
    }

    public void scheduleEventRideAssignment(SharedLink sharedLink) {
        if (sharedLink.getExpiresAt() == null) {
            log.warn("Cannot schedule ride assignment for event {} as it has no registration deadline",
                    sharedLink.getId());
            return;
        }

        // Convert the event's registration deadline to a cron expression.
        String cronExpression = getCronExpression(sharedLink.getExpiresAt());
        Trigger trigger = new CronTrigger(cronExpression);

        // Schedule the task using the TaskScheduler.
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            try {
                log.info("Starting ride assignment for event {}", sharedLink.getId());
                sharedLinkService.deletedAssociatedData(sharedLink);
                log.info("Completed ride assignment for event {}", sharedLink.getId());
            } catch (Exception e) {
                log.error("Error assigning rides for event {}", sharedLink.getId(), e);
            }
        }, trigger);

        log.info("Scheduled ride assignment for event {} at {}. Future: {}",
                sharedLink.getId(), sharedLink.getExpiresAt(), future);
    }
}
