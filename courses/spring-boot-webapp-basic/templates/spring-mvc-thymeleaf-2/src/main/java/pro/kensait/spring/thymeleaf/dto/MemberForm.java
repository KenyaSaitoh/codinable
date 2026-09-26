package pro.kensait.spring.thymeleaf.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
 * 会員登録フォームを表すクラス
 * th:field でバインドするため、フィールドにはsetter/getterが必要
 */
public class MemberForm {

    @NotBlank
    @Size(max = 20)
    private String memberName;

    @NotNull
    @Min(0)
    @Max(120)
    private Integer age;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String gender;

    @NotBlank
    private String prefecture;

    private List<String> hobbyList;

    @Size(max = 100)
    private String remarks;

    // 会員名称の取得
    public String getMemberName() {
        return memberName;
    }

    // 会員名称の設定
    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    // 年齢へのアクセサメソッド
    public Integer getAge() {
        return age;
    }

    // 年齢の設定
    public void setAge(Integer age) {
        this.age = age;
    }

    // メールアドレスの取得
    public String getEmail() {
        return email;
    }

    // メールアドレスの設定
    public void setEmail(String email) {
        this.email = email;
    }

    // 性別へのアクセサメソッド
    public String getGender() {
        return gender;
    }

    // 性別の設定
    public void setGender(String gender) {
        this.gender = gender;
    }

    // 都道府県の取得
    public String getPrefecture() {
        return prefecture;
    }

    // 都道府県の設定
    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    // hobby一覧の取得
    public List<String> getHobbyList() {
        return hobbyList;
    }

    // hobby一覧の設定
    public void setHobbyList(List<String> hobbyList) {
        this.hobbyList = hobbyList;
    }

    // 備考の取得
    public String getRemarks() {
        return remarks;
    }

    // 備考の設定
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "MemberForm [memberName=" + memberName + ", age=" + age
                + ", email=" + email + ", gender=" + gender
                + ", prefecture=" + prefecture + ", hobbyList=" + hobbyList
                + ", remarks=" + remarks + "]";
    }
}
