package pro.kensait.spring.calc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pro.kensait.spring.calc.config.ConfigHolder;

/*
 * 計算機能のビジネスロジックを表すクラス
 */
@Service
public class CalcService {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            CalcService.class);

    // インジェクションポイント
    @Autowired
    private ConfigHolder config;

    // サービスメソッド：足し算の実行
    public BigDecimal add(BigDecimal param1, BigDecimal param2) {
        LOGGER.info("[ CalcService#add ]");

        // 足し算を実行し、四捨五入してスケールを合わせる
        BigDecimal result = param1.add(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
        return result;
    }

    // サービスメソッド：引き算の実行
    public BigDecimal subtract(BigDecimal param1, BigDecimal param2) {
        LOGGER.info("[ CalcService#subtract ]");

        // 引き算を実行し、四捨五入してスケールを合わせる
        BigDecimal result = param1.subtract(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);
        return result;
    }
}
