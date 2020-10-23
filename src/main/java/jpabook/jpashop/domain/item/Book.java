package jpabook.jpashop.domain.item;

import jpabook.jpashop.controller.BookForm;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * 상품 - 도서 엔티티
 */
@Entity
@DiscriminatorValue("B") //값을 명시하지 않으면 클래스명이 기본값
@Getter
@Setter
public class Book extends Item {

    private String author;
    private String isbn;

    protected Book() {
    }

    public static Book createFromBookForm(BookForm form) {
        Book book = new Book();
        book.setId((form.getId())); //수정 시 기존값, 추가 시 null값
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        return book;
    }


}
