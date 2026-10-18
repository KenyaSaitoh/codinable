package pro.kensait.jakartaee.facelets.dto;

/** 会員（表示用の値を運ぶ型。データベースへ保存するエンティティではない） */
public record Member(
        Integer memberId,
        String memberName,
        Integer age,
        String email,
        Boolean admin) {
}
