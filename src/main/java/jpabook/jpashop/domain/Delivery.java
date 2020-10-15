package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import static javax.persistence.FetchType.LAZY;

/**
 * 배송 엔티티
 */

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = LAZY) //일대일, 하나의 배송정보도 꼭 하나의 주문정보만 가져야된다
    private Order order;

    @Embedded //내장타입 사용
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; //배송 상태 ENUM [READY(준비), COMP(배송)]

}
