package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

/**
 * 카테고리 엔티티
 */
@Entity
@Getter @Setter
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name; //카테고리명

    @ManyToMany //카테고리도 List로 아이템을 갖는다
    @JoinTable(name = "category_item", //중간 테이블 매핑, 다대다 예시일 뿐 실무에서는 @ManyToMany를 사용하지 X
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();
    /*
    실무에서는 @ManyToMany를 사용 X
    -> 중간 테이블에 등록일, 수정일 등 더이상의 컬럼 추가가 X, 세밀하게 쿼리 실행하기 어렵 -> 실무에서 사용의 한계
    바른 : 중간 엔티티(`CategoryItem`)을 만들고 다대다 매핑을 다대일(`@ManyToOne`), 일대다(`@OneToMany`) 매핑으로 풀어내서 사용
     */

    //카테고리 계층 구조 : 부모(상위 카테고리), 자식(하위 카테고리)
    //같은 엔티티 타입에 대해서 `셀프로 양방향 연관관계` 걸 수 있음
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent; //부모 타입도 내 타입과 같고 하나만 가질 수 있다

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>(); //내 자식은 여러 개를 가질 수 있다

    //==연관관계 메서드==/
    public void addChildCategory(Category child) {
        this.child.add(child); //부모에 자식을 짚어넣고
        child.setParent(this); //자식에도 부모가 누군지를 바로 볼수있게 짚어넣고
    }

//    실험
//    public void addItem(Item item) {
//        this.items.add(item);
//        item.getCategories().add(this);
//    }

}
