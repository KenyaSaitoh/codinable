package pro.kensait.spring.calc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pro.kensait.spring.calc.config.ConfigHolder;

/*
 * 税金計算のビジネスロジックを表すクラス
 */
@Service
public class TaxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            TaxService.class);

    // インジェクションポイント
    @Autowired
    private ConfigHolder config;

    // 消費税込みの金額を計算
    public BigDecimal calcTax(BigDecimal amount) {
        LOGGER.info("[ CalcService#calcTax ]");

        BigDecimal taxRate = new BigDecimal(config.getTaxRate());
        BigDecimal result = amount.multiply(taxRate)
                .setScale(0, RoundingMode.DOWN);
        return result;
    }
}
