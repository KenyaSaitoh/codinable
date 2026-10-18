package pro.kensait.jakartaee.calc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.jakartaee.calc.config.CalcConfig;
import pro.kensait.jakartaee.calc.service.exception.LimitOverException;
import pro.kensait.jakartaee.calc.service.exception.ZeroDivideException;

/** 計算と履歴の作成を受け持ち、保存した履歴の ID を返す */
@ApplicationScoped
public class CalcService {
    @Inject
    private CalcConfig config;

    @Inject
    private CalcDAO dao;

    public int add(BigDecimal param1, BigDecimal param2) {
        BigDecimal result = param1.add(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
        return save(param1, param2, CalcType.ADD, result);
    }

    public int subtract(BigDecimal param1, BigDecimal param2) {
        BigDecimal result = param1.subtract(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
        return save(param1, param2, CalcType.SUBTRACT, result);
    }

    // 保存してから上限を判定する。上限を超えた場合も、履歴には結果が残る（チャプター7.1「検証と保存の順序」）
    public int multiply(BigDecimal param1, BigDecimal param2)
            throws LimitOverException {
        BigDecimal result = param1.multiply(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
        int id = save(param1, param2, CalcType.MULTIPLY, result);
        BigDecimal limit = new BigDecimal(config.getCalcResultLimit());
        if (0 < result.compareTo(limit)) {
            throw new LimitOverException();
        }
        return id;
    }

    public int divide(BigDecimal param1, BigDecimal param2)
            throws ZeroDivideException {
        if (param2.compareTo(BigDecimal.ZERO) == 0) {
            throw new ZeroDivideException();
        }
        BigDecimal result = param1.divide(param2, config.getCalcResultScale(),
                RoundingMode.HALF_UP);
        return save(param1, param2, CalcType.DIVIDE, result);
    }

    public CalcResult getCalcResult(int id) {
        return dao.find(id);
    }

    public List<CalcResult> getCalcResults() {
        return dao.findAll();
    }

    private int save(BigDecimal param1, BigDecimal param2, CalcType calcType,
            BigDecimal result) {
        CalcResult calc = new CalcResult(param1, param2, calcType, result,
                LocalDateTime.now());
        dao.save(calc);
        return calc.getId();
    }
}
