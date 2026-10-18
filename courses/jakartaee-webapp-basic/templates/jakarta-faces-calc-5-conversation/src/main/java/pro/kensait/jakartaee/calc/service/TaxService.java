package pro.kensait.jakartaee.calc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.jakartaee.calc.config.CalcConfig;

/** 税込金額（税率 calc.tax.rate を掛けて、1 円未満を切り捨てる）を求める */
@ApplicationScoped
public class TaxService {
    @Inject
    private CalcConfig config;

    public BigDecimal calcTax(BigDecimal amount) {
        return amount.multiply(config.getTaxRate())
                .setScale(0, RoundingMode.DOWN);
    }
}
