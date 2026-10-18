package pro.kensait.jakartaee.facelets.web;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import pro.kensait.jakartaee.facelets.dto.Member;

/** 会員の一覧を渡すバッキングBean（CollectionPage.xhtml・LinkPage.xhtml） */
@Named
@ApplicationScoped
public class CollectionBean {
    private final List<Member> memberList = List.of(
            new Member(1, "Alice", 28, "alice@example.com", true),
            new Member(2, "Bob", 35, "bob@example.com", false),
            new Member(3, "Carol", 42, "carol@example.com", false));

    public List<Member> getMemberList() {
        return memberList;
    }

    public Member findMember(Integer memberId) {
        return memberList.stream()
                .filter(m -> m.memberId().equals(memberId))
                .findFirst()
                .orElse(null);
    }
}
