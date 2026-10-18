package pro.kensait.jakartaee.calc.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CalcConfig {
    @Inject
    @ConfigProperty(name = "calc.result.scale")
    private Integer calcResultScale;

    @Inject
    @ConfigProperty(name = "calc.result.limit")
    private Integer calcResultLimit;

    public Integer getCalcResultScale() {
        return calcResultScale;
    }

    public Integer getCalcResultLimit() {
        return calcResultLimit;
    }
}
