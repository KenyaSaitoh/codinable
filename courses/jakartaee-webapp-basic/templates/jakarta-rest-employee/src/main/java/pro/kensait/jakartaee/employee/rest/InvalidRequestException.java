package pro.kensait.jakartaee.employee.rest;

/** 形式は正しいが、既存データと整合しない要求（マスターが無い、version が無いなど） */
public class InvalidRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        super(message);
    }
}
