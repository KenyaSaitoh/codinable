package pro.kensait.jakartaee.calc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.jakartaee.calc.config.CalcConfig;

@ApplicationScoped
public class CalcService {
    @Inject
    private CalcConfig config;

    public BigDecimal add(BigDecimal param1, BigDecimal param2) {
        return param1.add(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
    }
}
