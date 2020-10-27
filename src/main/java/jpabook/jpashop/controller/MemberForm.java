package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

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

    /**
     * Create dto instance from entity instance
     * @param entity
     * @return created dto instance
     */
    public static MemberForm valueOf(Member entity) {
            MemberForm dto = new MemberForm();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setCity(entity.getAddress().getCity());
            dto.setStreet(entity.getAddress().getStreet());
            dto.setZipcode(entity.getAddress().getZipcode());
        return dto;
    }


}
