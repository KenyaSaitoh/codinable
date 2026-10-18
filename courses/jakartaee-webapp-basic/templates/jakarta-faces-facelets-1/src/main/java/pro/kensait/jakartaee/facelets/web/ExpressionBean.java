package pro.kensait.jakartaee.facelets.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/** 式とテキスト出力の画面（ExpressionPage.xhtml）のバッキングBean */
@Named
@RequestScoped
public class ExpressionBean {
    private String memberName = "Alice";
    private Integer price = 1200;
    private Boolean admin = true;
    // 値が null のときの表示を確かめるため、ニックネームは設定しない
    private String nickname = null;
    // エスケープの有無を確かめるための、タグを含む文字列
    private String htmlText = "<strong>強調されたテキスト</strong>";

    public String getMemberName() {
        return memberName;
    }

    public Integer getPrice() {
        return price;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public String getNickname() {
        return nickname;
    }

    public String getHtmlText() {
        return htmlText;
    }
}
