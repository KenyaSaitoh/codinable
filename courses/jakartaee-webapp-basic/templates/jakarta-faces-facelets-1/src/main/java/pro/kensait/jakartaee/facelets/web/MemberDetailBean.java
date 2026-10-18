package pro.kensait.jakartaee.facelets.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pro.kensait.jakartaee.facelets.dto.Member;

/**
 * 会員詳細の画面（MemberDetailPage.xhtml）のバッキングBean
 * リンクの f:param で渡った memberId を、画面の f:viewParam が setMemberId で設定する
 * （ビューパラメータの仕組みはチャプター6.1で取り上げる）
 */
@Named
@RequestScoped
public class MemberDetailBean {
    @Inject
    private CollectionBean collectionBean;

    private Integer memberId;

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Member getMember() {
        return memberId == null ? null : collectionBean.findMember(memberId);
    }
}
