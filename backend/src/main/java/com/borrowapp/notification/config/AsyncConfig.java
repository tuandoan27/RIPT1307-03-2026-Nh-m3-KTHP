package com.borrowapp.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cấu hình thread pool riêng cho email async.
 * Tách biệt khỏi HTTP request thread pool để tránh tranh chấp tài nguyên.
 */
@EnableAsync
@EnableScheduling
@Configuration
public class AsyncConfig {

    /**
     * Thread pool dành riêng cho tác vụ gửi email.
     * - corePoolSize = 2   : luôn có 2 thread sẵn sàng
     * - maxPoolSize  = 10  : tối đa 10 thread khi tải cao
     * - queueCapacity= 500 : hàng đợi trước khi scale lên max
     * - awaitTermination   : chờ tối đa 30s khi shutdown
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
