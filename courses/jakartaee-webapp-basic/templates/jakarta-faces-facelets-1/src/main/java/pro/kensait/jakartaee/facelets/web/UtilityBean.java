package pro.kensait.jakartaee.facelets.web;

import java.time.LocalDateTime;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/** 表示形式（f:convertNumber・f:convertDateTime）を確かめる値 */
@Named
@RequestScoped
public class UtilityBean {
    private Integer bigNumber = 1000000;
    private LocalDateTime now = LocalDateTime.now();

    public Integer getBigNumber() {
        return bigNumber;
    }

    public LocalDateTime getNow() {
        return now;
    }
}
