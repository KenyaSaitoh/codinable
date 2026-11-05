package pro.kensait.spring.employee.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/*
 * @Async を有効にし、社員の処理専用のエグゼキューターを定義する
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean
    public SimpleAsyncTaskExecutor employeeExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("employee-"); // 【1】
        executor.setVirtualThreads(true); // 【2】 タスクごとに仮想スレッドを作る
        executor.setConcurrencyLimit(8); // 【3】 同時に動かすタスクの上限
        return executor;
    }

    // 戻り値が void の @Async メソッドの例外は呼び出し元に届かないので、ここで受け取る
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (error, method, params) -> logger.warn("異動通知の一部が失敗しました method={}", method.getName(), error);
    }
}
