package com.bistro.infrastructure;

import com.bistro.domain.model.discount.DiscountStrategy;
import com.bistro.domain.model.discount.HappyHourStrategy;
import com.bistro.domain.model.discount.LargeOrderStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class InfrastructureConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public DiscountStrategy happyHourStrategy(Clock clock) {
        return new HappyHourStrategy(clock);
    }

    @Bean
    public DiscountStrategy largeOrderStrategy() {
        return new LargeOrderStrategy();
    }
}
