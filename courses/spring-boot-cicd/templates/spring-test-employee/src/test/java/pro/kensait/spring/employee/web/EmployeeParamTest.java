package pro.kensait.spring.employee.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/*
 * 社員パラメータのテスト
 */
class EmployeeParamTest {
    // 有効パラメータの実行
    private EmployeeParam validParam() {
        EmployeeParam param = new EmployeeParam();
        param.setEmployeeName("  Alice  ");
        param.setDepartmentId(1);
        param.setJobId(2);
        param.setSalary("300000");
        param.setEntranceDate("2026-04-01");
        return param;
    }

    // 「有効な入力から社員への変換」の検証
    @Test
    void convertsValidInputToEmployee() {
        EmployeeParam param = validParam();
        assertThat(param.validate(true, true)).isEmpty();
        var employee = param.toEmployee();
        assertThat(employee.getEmployeeName()).isEqualTo("Alice");
        assertThat(employee.getDepartmentId()).isEqualTo(1);
        assertThat(employee.getJobId()).isEqualTo(2);
        assertThat(employee.getSalary()).isEqualTo(300000);
        assertThat(employee.getEntranceDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    // 「全未入力項目の通知」の検証
    @Test
    void reportsAllMissingFields() {
        assertThat(new EmployeeParam().validate(false, false)).containsExactly(
                "氏名を入力してください", "部署を選んでください", "役職を選んでください",
                "月給は0以上9999999以下で入力してください", "入社日を入力してください");
    }

    // 「不正な月給の拒否」の検証
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"-1", "10000000", "300,000", "abc", "2147483648"})
    void rejectsInvalidSalary(String salary) {
        EmployeeParam param = validParam();
        param.setSalary(salary);
        assertThat(param.validate(true, true)).containsExactly("月給は0以上9999999以下で入力してください");
    }

    // 「月給の境界値の受付」の検証
    @ParameterizedTest
    @ValueSource(strings = {"0", "9999999", " 300000 "})
    void acceptsSalaryBoundaries(String salary) {
        EmployeeParam param = validParam();
        param.setSalary(salary);
        assertThat(param.validate(true, true)).isEmpty();
    }

    // 「不正な日付の拒否」の検証
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2026-02-30", "2026/04/01", "invalid"})
    void rejectsInvalidDate(String date) {
        EmployeeParam param = validParam();
        param.setEntranceDate(date);
        assertThat(param.validate(true, true)).containsExactly("入社日を入力してください");
    }

    // 「氏名の文字数とマスター選択の制約」の検証
    @Test
    void enforcesNameLengthAndMasterSelections() {
        EmployeeParam param = validParam();
        param.setEmployeeName("あ".repeat(30));
        assertThat(param.validate(true, true)).isEmpty();
        param.setEmployeeName("あ".repeat(31));
        assertThat(param.validate(false, false)).containsExactly(
                "氏名は30文字以内で入力してください", "部署を選んでください", "役職を選んでください");
    }
}
