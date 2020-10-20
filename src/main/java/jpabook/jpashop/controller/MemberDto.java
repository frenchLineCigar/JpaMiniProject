package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

/**
 * 회원 데이터 전송 객체
 */
@Getter @Setter
public class MemberDto {

    private Long id;
    private String name;

    private String city;
    private String street;
    private String zipcode;

}
