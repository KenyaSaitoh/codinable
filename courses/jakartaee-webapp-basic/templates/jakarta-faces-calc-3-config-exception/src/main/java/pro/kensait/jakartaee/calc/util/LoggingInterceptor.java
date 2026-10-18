package pro.kensait.jakartaee.calc.util;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {
    @Inject
    private Logger logger;

    @AroundInvoke
    public Object log(InvocationContext context) throws Exception {
        Method method = context.getMethod();
        String name = method.getDeclaringClass().getSimpleName()
                + "#" + method.getName();
        logger.info("[ " + name + " ] start");
        long start = System.nanoTime();
        try {
            return context.proceed();
        } finally {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            logger.info("[ " + name + " ] end " + elapsed + "ms");
        }
    }
}
