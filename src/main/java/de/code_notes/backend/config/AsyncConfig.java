package de.code_notes.backend.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import de.code_notes.backend.controllers.CustomExceptionHandler;
import lombok.extern.log4j.Log4j2;


/**
 * Class configuring the behaviour of asynchronous methods.
 * 
 * @since 0.0.1
 * @author Florin Schikarski
 */
@Configuration
@Log4j2
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }


    /**
     * Will log exceptions thrown in any method annotated with {@code @Async} (wont work for methods that use
     * a plain java {@code Thread}).
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {

                new CustomExceptionHandler().handleException((Exception) ex);
            }
        };
    }
}