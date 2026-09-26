package pro.kensait.spring.thymeleaf.dto;

/*
 * テンプレートに渡すデータ（会員）を表すクラス
 */
public record Member(Integer memberId, String memberName, Integer age) {
}
