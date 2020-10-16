package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

/**
 * 주문 엔티티
 */

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = EAGER) //다대일, 하나의 주문에는 한명의 주문 회원만 존재할 수 있음
    @JoinColumn(name = "member_id") //매핑을 뭘로 할 것인지, FK : member_id
    private Member member; //주문 회원

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //일대다, 하나의 주문이 여러개의 주문상품을 가질 수 있음
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL) //일대일, 하나의 주문은 꼭 하나의 배송정보만 가져야 된다
    @JoinColumn(name = "delivery_id")
    private Delivery delivery; //배송정보

    private LocalDateTime orderDate; //주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    //==연관관계 메서드 : 양방향에서는 이 메서드가 있는 것이 좋고, 원자적으로 묶어 한 코드로 양쪽 다 해결, 핵심적으로 컨트롤 하는 쪽에 위치 하는 것이 좋음 ==/
    public void setMember(Member member) { /* Member와 양방향 */
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) { /* OrderItem도 양방향 */
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) { /* Delivery도 양방향 */
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    // 주문 회원, 배송정보, 주문상품의 정보를 받아서 실제 주문 엔티티를 생성한다 (여러 연관관계가 들어가고 복잡한 생성은 이렇게 별도의 생성 메서드를 두는 것이 좋다)
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER); //주문 상태: 처음 상태는 ORDER로 강제해놓는다
        order.setOrderDate(LocalDateTime.now()); //주문 시간 정보: 현재 시간으로 잡는다
        return order; //연관관계를 쫙 걸면서 셋팅된 order를 반환
    }

    //==비즈니스 로직==//

    /**
     * 주문 취소: 주문 취소시 사용한다. 주문 상태를 취소로 변경하고 주문상품에 주문 취소를 알린다. 주문 취소 수량만큼 해당 상품의 재고를 다시 늘려야 하고, 만약 이미 배송을 완료한 상품이면 주문을 취소하지 못하도록 예외를 발생시킨다.
     */
    public void cancel() {
        //validation logic
        if (delivery.getStatus() == DeliveryStatus.COMP) { //배송이 이미 완료 됐으면 주문 취소가 불가능 해!
            throw new IllegalStateException("이미 배송된 상품은 취소가 불가능합니다.");
        }

        //validation을 통과하면
        this.setStatus(OrderStatus.CANCEL); //이 주문의 상태를 CANCEL로 바꿔준고
        //해당 상품의 재고를 원상 복구한다
        for (OrderItem orderItem : orderItems) { //현재 주문이 가진 각각의 주문 상품을 꺼내서 캔슬시킨다
            orderItem.cancel();
        }
    }

    //==조회 로직==//

    /**
     * 전체 주문 가격 조회: 연관된 주문상품들의 가격을 조회해서 더한 값을 반환한다
     */
    private int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
// java 8
//        return orderItems.stream()
//                .mapToInt(OrderItem::getTotalPrice)
//                .sum();
    }


}
