package com.janpeterdhalle.transfer.config;

import com.janpeterdhalle.transfer.eventhandlers.SharedLinkEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguation {
    @Bean
    SharedLinkEventHandler eventHandler() {
        return new SharedLinkEventHandler();
    }
}
