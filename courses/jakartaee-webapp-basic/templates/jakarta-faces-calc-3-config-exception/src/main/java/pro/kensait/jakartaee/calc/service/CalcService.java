package pro.kensait.jakartaee.calc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.jakartaee.calc.config.CalcConfig;
import pro.kensait.jakartaee.calc.service.exception.LimitOverException;
import pro.kensait.jakartaee.calc.service.exception.ZeroDivideException;
import pro.kensait.jakartaee.calc.util.Logged;

@Logged
@ApplicationScoped
public class CalcService {
    @Inject
    private CalcConfig config;

    public BigDecimal add(BigDecimal param1, BigDecimal param2) {
        return param1.add(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
    }

    public BigDecimal subtract(BigDecimal param1, BigDecimal param2) {
        return param1.subtract(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
    }

    // 丸める前の積を上限と比べ、超えていれば LimitOverException で通知する（画面名は知らない）
    public BigDecimal multiply(BigDecimal param1, BigDecimal param2)
            throws LimitOverException {
        BigDecimal tmpResult = param1.multiply(param2);
        BigDecimal limit = new BigDecimal(config.getCalcResultLimit());
        if (0 < tmpResult.compareTo(limit)) {
            throw new LimitOverException();
        }
        return tmpResult.setScale(config.getCalcResultScale(),
                RoundingMode.HALF_UP);
    }

    public BigDecimal divide(BigDecimal param1, BigDecimal param2)
            throws ZeroDivideException {
        if (param2.compareTo(BigDecimal.ZERO) == 0) {
            throw new ZeroDivideException();
        }
        return param1.divide(param2, config.getCalcResultScale(),
                RoundingMode.HALF_UP);
    }
}
