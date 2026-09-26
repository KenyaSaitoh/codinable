package pro.kensait.spring.employee.web;

import java.io.Serializable;
import java.util.UUID;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** 確定前の入力だけを保持する。JPAエンティティはセッションに格納しない。 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP",
        justification = "The MVC draft intentionally shares its mutable input DTO within one edit flow")
public class EmployeeDraft implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String token = UUID.randomUUID().toString();
    private EmployeeParam input;
    private boolean confirmed;

    public EmployeeDraft(EmployeeParam input) {
        this.input = input;
    }
    public String getToken() {
        return token;
    }
    public EmployeeParam getInput() {
        return input;
    }
    public void setInput(EmployeeParam input) {
        this.input = input;
    }
    public boolean isConfirmed() {
        return confirmed;
    }
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
