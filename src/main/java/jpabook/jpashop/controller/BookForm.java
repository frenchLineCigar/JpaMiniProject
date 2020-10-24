package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * 상품 등록 > Book 폼 객체 (유효성 검사)
 */
@Getter @Setter
public class BookForm {

    private Long id;

    @NotEmpty(message = "상품 이름은 필수 입니다")
    private String name;

    @NotNull
    @PositiveOrZero(message = "상품 가격은 0 또는 양수")
    private int price;

    @NotNull
    @PositiveOrZero(message = "상품 수량은 0 또는 양수")
    private int stockQuantity;

    private String author; //저자
    private String isbn; //ISBN

    /* ENTITY to DTO */
    public static BookForm mapping(Book book) {
        // getter,setter치기 귀찮음 -> ModelMapper 라이브러리나 인텔리J의 멀티 라인 셀렉트 플러그인 등을 찾아보고 활용해보자
        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setName(book.getName());
        form.setPrice(book.getPrice());
        form.setStockQuantity(book.getStockQuantity());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());
        return form;
    }

}
