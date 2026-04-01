package com.product.ground_control.analytics.application.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async configuration for analytics event ingestion.
 *
 * <p><b>Virtual Thread Executor:</b> Uses {@link Executors#newVirtualThreadPerTaskExecutor()}
 * to spawn lightweight virtual threads for each async task. Virtual threads are cheaper than
 * platform threads, allowing higher concurrency without resource exhaustion.
 *
 * <p><b>Bounded Concurrency (Bulkhead):</b> A semaphore with 100 permits caps the number of
 * concurrent ingestion tasks. When the limit is reached, new tasks queue and wait (backpressure).
 * This prevents cascading failures under high load and protects downstream resources.
 *
 * <p><b>Observability:</b> Logs task acquisition, release, and queue depth for monitoring
 * and debugging async behavior.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    private static final int SEMAPHORE_PERMITS = 100;

    /**
     * Virtual thread executor with semaphore-based bulkhead for bounded concurrency.
     *
     * @return Executor wrapping virtual threads with semaphore backpressure
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        final Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        final Semaphore semaphore = new Semaphore(SEMAPHORE_PERMITS);

        return task -> {
            virtualThreadExecutor.execute(() -> {
                try {
                    // Acquire semaphore permit (blocks if all 100 are in use)
                    semaphore.acquire();
                    int availablePermits = semaphore.availablePermits();
                    log.debug("Async task acquired semaphore [available={}, total={}]",
                        availablePermits, SEMAPHORE_PERMITS);

                    // Execute the task
                    task.run();

                } catch (InterruptedException e) {
                    // Thread was interrupted while waiting for semaphore
                    log.warn("Async task interrupted while waiting for semaphore", e);
                    Thread.currentThread().interrupt();
                } finally {
                    // Always release the permit, even if task fails
                    semaphore.release();
                    int availablePermits = semaphore.availablePermits();
                    log.debug("Async task released semaphore [available={}, total={}]",
                        availablePermits, SEMAPHORE_PERMITS);
                }
            });
        };
    }
}
