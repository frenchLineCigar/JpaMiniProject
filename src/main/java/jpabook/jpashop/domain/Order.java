package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

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

    @OneToMany(mappedBy = "order") //일대다, 하나의 주문이 여러개의 주문상품을 가질 수 있음
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY) //일대일, 하나의 주문은 꼭 하나의 배송정보만 가져야 된다
    @JoinColumn(name = "delivery_id")
    private Delivery delivery; //배송정보

    private LocalDateTime orderDate; //주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

}
