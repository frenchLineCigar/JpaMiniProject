package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 데이터 전송 객체
 * 회원 가입 > 폼 객체 (유효성 검사)
 * 회원 목록
 */
@Getter @Setter
public class MemberForm {

    private Long id;

    @NotEmpty(message = "회원 이름은 필수 입니다")
    private String name; //이름은 필수값

    private String city;
    private String street;
    private String zipcode;

    /* ENTITY to DTO */
    public static List<MemberForm> convertMemberList(List<Member> members) {
        List<MemberForm> memberList = new ArrayList<>();
        members.forEach(member -> {
            MemberForm m = new MemberForm();
            m.setId(member.getId());
            m.setName(member.getName());
            m.setCity(member.getAddress().getCity());
            m.setStreet(member.getAddress().getStreet());
            m.setZipcode(member.getAddress().getZipcode());
            memberList.add(m);
        });
        return memberList;
    }

}
