package pro.kensait.leafbooks.api.dto;

/**
 * 社員ログインレスポンスDTO
 */
public record EmployeeLoginResponse(
    Long employeeId,
    String employeeCode,
    String employeeName,
    String email,
    Integer jobRank,   // 1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR
    Long departmentId,
    String departmentName
) {
}
