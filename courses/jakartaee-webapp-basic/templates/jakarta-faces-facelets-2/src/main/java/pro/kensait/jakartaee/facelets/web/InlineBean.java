package pro.kensait.jakartaee.facelets.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/** JavaScript へ値を渡す画面（InlinePage.xhtml）のバッキングBean */
@Named
@RequestScoped
public class InlineBean {
    private String memberName = "Alice";

    public String getMemberName() {
        return memberName;
    }
}
