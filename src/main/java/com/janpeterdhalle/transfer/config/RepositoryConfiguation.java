package com.janpeterdhalle.transfer.config;

import com.janpeterdhalle.transfer.eventhandlers.SharedLinkEventHandler;
import com.janpeterdhalle.transfer.eventhandlers.UserEventHandler;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import com.janpeterdhalle.transfer.services.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfiguation {
    private final SchedulingService schedulingService;
    private final SharedLinkRepository sharedLinkRepository;

    @Bean
    SharedLinkEventHandler eventHandler() {
        return new SharedLinkEventHandler(schedulingService);
    }

    @Bean
    UserEventHandler userEventHandler() {
        return new UserEventHandler(sharedLinkRepository);
    }
}
