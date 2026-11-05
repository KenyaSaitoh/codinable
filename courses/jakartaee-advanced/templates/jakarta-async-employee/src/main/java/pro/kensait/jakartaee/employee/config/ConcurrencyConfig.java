package pro.kensait.jakartaee.employee.config;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.ALL_REMAINING;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.SECURITY;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.context.ApplicationScoped;

/*
 * 社員の処理専用の ContextService と ManagedExecutorService の定義
 * 管理コンソールでの設定なしに、アプリケーションと一緒に配布できる（java:app の名前はこのアプリケーションの中で有効）
 */
@ContextServiceDefinition(
        name = "java:app/concurrent/EmployeeContextService",
        propagated = { APPLICATION, SECURITY }, // 【1】
        cleared = TRANSACTION, // 【2】
        unchanged = ALL_REMAINING)
@ManagedExecutorDefinition(
        name = "java:app/concurrent/EmployeeExecutor",
        context = "java:app/concurrent/EmployeeContextService",
        maxAsync = 8, // 【3】
        virtual = true) // 【4】
@ApplicationScoped
public class ConcurrencyConfig {
}
