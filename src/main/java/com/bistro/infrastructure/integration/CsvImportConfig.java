package com.bistro.infrastructure.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.inbound.FileReadingMessageSource;

import java.io.File;
import java.util.List;

@Configuration
@EnableIntegration
public class CsvImportConfig {

    @Value("${bistro.csv.import.directory}")
    private String importDirectory;

    @Value("${bistro.csv.poll.interval.ms:3600000}")
    private long pollIntervalMs;

    @Bean
    public FileReadingMessageSource csvFileSource() {
        var filters = new CompositeFileListFilter<>(List.of(
            new SimplePatternFileListFilter("*.csv"),
            new AcceptOnceFileListFilter<>()
        ));
        var source = new FileReadingMessageSource();
        source.setDirectory(new File(importDirectory));
        source.setFilter(filters);
        return source;
    }

    @Bean
    public IntegrationFlow csvImportFlow(FileReadingMessageSource csvFileSource,
                                         ProductCsvImportHandler handler) {
        return IntegrationFlow
            .from(csvFileSource, spec -> spec.poller(Pollers.fixedDelay(pollIntervalMs)))
            .handle(message -> handler.handle((File) message.getPayload()))
            .get();
    }
}
