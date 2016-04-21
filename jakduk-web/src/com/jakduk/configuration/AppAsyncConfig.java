package com.jakduk.configuration;

import com.jakduk.trigger.TokenTerminationTrigger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Created by pyohwan on 16. 4. 2.
 */

@Configuration
@EnableAsync
public class AppAsyncConfig implements AsyncConfigurer {

    @Bean(initMethod = "init")
    public TokenTerminationTrigger tokenTerminationTrigger() {
        TokenTerminationTrigger tokenTerminationTrigger = new TokenTerminationTrigger();
        tokenTerminationTrigger.setSpan(5);

        return tokenTerminationTrigger;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(2);
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
