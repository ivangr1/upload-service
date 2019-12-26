package com.infobip.uploadservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class UploadConfiguration {

    @Bean
    public ExecutorService executor() {
        return Executors.newFixedThreadPool(100);
    }
}
