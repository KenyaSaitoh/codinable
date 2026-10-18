package pro.kensait.jakartaee.facelets.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/** メッセージの {0} に埋め込む値を渡すバッキングBean（MessagePage.xhtml） */
@Named
@RequestScoped
public class MessageBean {
    private String memberName = "Alice";

    public String getMemberName() {
        return memberName;
    }
}
