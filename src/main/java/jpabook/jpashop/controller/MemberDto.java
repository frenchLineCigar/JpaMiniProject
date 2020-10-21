package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 회원 데이터 전송 객체
 */
@Getter @Setter
public class MemberDto {

    @NotBlank
    private Long id;
    @NotBlank
    private String name;

    private String city;
    private String street;
    private String zipcode;

}
