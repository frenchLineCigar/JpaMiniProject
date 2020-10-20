package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;

/**
 * 상품 등록 > Book 폼 객체 (유효성 검사)
 */
@Getter @Setter
public class BookForm {

    private Long id;

    @NotEmpty(message = "상품 이름은 필수 입니다")
    private String name;
//    @NotEmpty(message = "상품 가격은 필수 입니다")
    @PositiveOrZero(message = "상품 가격은 0 또는 양수")
    private int price;
//    @NotEmpty(message = "상품 수량은 필수 입니다")
    @PositiveOrZero(message = "상품 수량은 0 또는 양수")
    private int stockQuantity;

    private String author; //저자
    private String isbn; //ISBN

}
