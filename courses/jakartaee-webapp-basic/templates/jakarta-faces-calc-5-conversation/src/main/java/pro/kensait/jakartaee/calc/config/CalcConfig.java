package pro.kensait.jakartaee.calc.config;

import java.math.BigDecimal;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CalcConfig {
    @Inject
    @ConfigProperty(name = "calc.result.scale")
    private Integer calcResultScale;

    @Inject
    @ConfigProperty(name = "calc.tax.rate")
    private BigDecimal taxRate;

    public Integer getCalcResultScale() {
        return calcResultScale;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }
}
