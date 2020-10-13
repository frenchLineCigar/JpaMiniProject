package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frenchline707@gmail.com on 2020-10-13
 * Blog : http://frenchline707.tistory.com
 * Github : http://github.com/frenchLineCigar
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //상속관계 전략지정은 싱글 테이블 전략으로
@DiscriminatorColumn(name = "dtype") //구분값
@Getter @Setter
public abstract class Item { //구현체 3개를 가지고 만들기 때문에 추상 클래스로로, 상속관계 매핑

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    //이름, 가격, 재고는 공통 속성
    private String name; //상품 이름
    private int price; //상품 가격
    private int stockQuantity; //재고 수량

    @ManyToMany(mappedBy = "items") //아이템도 List로 카테고리를 갖는다
    private List<Category> categories = new ArrayList<>();
}
