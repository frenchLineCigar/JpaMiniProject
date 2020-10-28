package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 + 배송정보 + 회원을 조회하는 API
 * 지연 로딩 때문에 발생하는 성능 문제를 단계적으로 해결
 */

/**
 * xToOne(ManyToOne, OneToOne) 관계의 성능 최적화
 * Order
 * Order -> Member : ManyToOne
 * Order -> Delivery : OneToOne
 * <p>
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

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 단점: 지연로딩(lazy loading)으로 인한 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        //ORDER 2개 (N = 2)
        //N + 1 문제 -> 1 + 회원 N + 배송 N
        List<Order> orders = orderRepository.findAll(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(result);
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     */
    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(result);
    }
    /**
     * QueryDSL을 적용한 V3
     */
    @GetMapping("/api/v3/simple-orders-querydsl")
    public Result ordersV3byQueryDSL(@RequestBody @Valid OrderSearch orderSearch) {
        List<Order> orders = orderRepository.findAllWithMemberDeliveryByQueryDSL(orderSearch);

        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(result);
    }

    /**
     * V4. JPA에서 DTO로 바로 조회
     * - 쿼리 1번 호출
     * - select 절에서 원하는 데이터만 선택해서 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderRepository.findOrderDtos();
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name; //주문자명
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus; //주문상태
        private Address address; //배송지 (고객주소가 x)

        public SimpleOrderDto(Order order) { //dto가 entity를 파라미터로 받는 건 크게 문제가 되지 않음. 별로 중요하지 않은 곳에서 중요한 것을 의존하는 것이기 때문에
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }


}
