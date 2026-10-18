package pro.kensait.jakartaee.facelets.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * 会員フォーム（FormPage.xhtml）のバッキングBean
 * 選択肢の Map は「表示ラベル → 送信値」。出力順を固定するため LinkedHashMap を使う
 * 選択肢はポストバックの検証でも使われるので、どのリクエストでも同じものを返す
 */
@Named
@RequestScoped
public class MemberFormBean {
    private static final Map<String, String> GENDER_MAP = new LinkedHashMap<>();
    private static final Map<String, String> PREFECTURE_MAP = new LinkedHashMap<>();
    private static final Map<String, String> HOBBY_MAP = new LinkedHashMap<>();

    static {
        GENDER_MAP.put("男性", "MALE");
        GENDER_MAP.put("女性", "FEMALE");
        GENDER_MAP.put("回答しない", "UNKNOWN");

        PREFECTURE_MAP.put("北海道", "HOKKAIDO");
        PREFECTURE_MAP.put("東京都", "TOKYO");
        PREFECTURE_MAP.put("大阪府", "OSAKA");
        PREFECTURE_MAP.put("福岡県", "FUKUOKA");

        HOBBY_MAP.put("読書", "READING");
        HOBBY_MAP.put("旅行", "TRAVEL");
        HOBBY_MAP.put("料理", "COOKING");
    }

    private String memberName;
    private Integer age;
    private String email;
    private String gender;
    private String prefecture;
    private List<String> hobbyList = new ArrayList<>();
    private String remarks;

    // 登録ボタンのアクションメソッド。同じリクエストの中で結果画面をレンダリングする
    public String register() {
        return "FormResultPage";
    }

    public Map<String, String> getGenderMap() {
        return GENDER_MAP;
    }

    public Map<String, String> getPrefectureMap() {
        return PREFECTURE_MAP;
    }

    public Map<String, String> getHobbyMap() {
        return HOBBY_MAP;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public List<String> getHobbyList() {
        return hobbyList;
    }

    public void setHobbyList(List<String> hobbyList) {
        this.hobbyList = hobbyList;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
