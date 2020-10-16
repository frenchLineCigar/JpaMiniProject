package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

/**
 * 주문상품 엔티티
 */

@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item; //주문 상품

    @ManyToOne(fetch = LAZY) //다대일, 주문상품은 하나의 주문만 가질 수 있음
    @JoinColumn(name = "order_id")
    private Order order; //주문

    private int orderPrice; //주문 가격 (시점에 따라 할인 쿠폰/할인 적용 등으로 상품 가격이 변동될 수 있으므로 orderPrice로 다르게 네이밍한다)
    private int count; //주문 수량

    //==생성 메서드==//
    // 주문 상품, 가격, 수량 정보를 사용해서 주문상품 엔티티를 생성한다
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        //주문된 만큼의 상품 재고를 까줘야 된다..
        item.removeStock(count);
        return orderItem;
    }

    //==비즈니스 로직==/

    /**
     * 주문 취소: 취소한 상품의 재고 수량을 원상복구 한다
     */
    public void cancel() {
        getItem().addStock(count); //item을 가져와서 취소한 주문 수량만큼 상품의 재고를 증가시킨다.
    }

    //==조회 로직==/

    /**
     * 주문상품 전체 가격 조회 : 주문 가격에 수량을 곱한 값을 반환한다
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
