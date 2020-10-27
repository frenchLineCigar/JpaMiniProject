package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 주문 + 배송정보 + 회원을 조회하는 API
 * 지연 로딩 때문에 발생하는 성능 문제를 단계적으로 해결
 */

/**
 * xToOne(ManyToOne, OneToOne) 관계의 성능 최적화
 * Order
 * Order -> Member : ManyToOne
 * Order -> Delivery : OneToOne
 * 
 * 양방향 연관 관계 문제 해결
 * 프록시 객체 문제
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
    */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화 (getMember()까진 프록시 객체, getName()부터 강제 초기화)
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }
}
