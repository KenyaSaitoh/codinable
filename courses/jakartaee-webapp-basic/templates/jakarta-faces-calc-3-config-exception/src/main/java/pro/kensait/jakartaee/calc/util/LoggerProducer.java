package pro.kensait.jakartaee.calc.util;

import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class LoggerProducer {
    @Produces
    public Logger createLogger(InjectionPoint injectionPoint) {
        Class<?> owner = injectionPoint.getMember().getDeclaringClass();
        return Logger.getLogger(owner.getName());
    }
}
