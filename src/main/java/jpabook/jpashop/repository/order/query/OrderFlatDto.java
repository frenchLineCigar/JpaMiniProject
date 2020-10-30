package jpabook.jpashop.repository.order.query;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 진짜 그냥 DB에서 쿼리 한방에 다 가져오도록
 * Order과 OrderItem 조인하고, OrderItem과 Item 조인해서 딱 한방 쿼리로 다 가져와야 하기 때문에
 * SQL JOIN은 N쪽에 맞춰서 데이터를 한줄로 만들면서 뻥튀기 될 수 밖에 없다.
 * 데이터가 진짜 한줄로 FLAT하게 하면서도 SQL JOIN의 결과를 그대로 가져올 수 있도록 데이터 구조를 맞춰야 한다
 */
@Data
public class OrderFlatDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    private String itemName; //상품 명
    private int orderPrice; //주문 가격
    private int count; //주문 수량

    public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
