package com.infobip.uploadservice;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.concurrent.Executor;

@org.springframework.context.annotation.Configuration
@EnableAsync
public class Configuration {

    // Ovo je Excecutor u kojem se definiraju parametri za threadove kojeg koristim u kontroleru (UploadController) za pokretanje threadova
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setThreadNamePrefix("UploadThread-");
        executor.initialize();
        return executor;
    }

    // Ovo zamjenjuje Springov defaultni resolver za upload fileova sa Apache Commonsovim FileUpload-om jer on podržava upload velikih fajlova i druge razne stvari
    @Bean
    public CommonsMultipartResolver multipartResolver() {

        CommonsMultipartResolver cmr = new CommonsMultipartResolver();
        cmr.setMaxInMemorySize(Integer.MAX_VALUE);
        cmr.setMaxUploadSize(Integer.MAX_VALUE);
        cmr.setMaxUploadSizePerFile(Integer.MAX_VALUE);
        return cmr;

    }
}
