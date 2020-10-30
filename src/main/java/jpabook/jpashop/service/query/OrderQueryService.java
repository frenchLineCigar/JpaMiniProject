package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 *
 * API나 화면 처리를 위한 쿼리용 서비스를 별도로 분리
 * 
 * * OrderService
 *     * OrderService: 핵심 비즈니스 로직
 *     * OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
 *
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) //조회용 이므로 읽기 전용 트랜잭션 사용
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * OSIV를 OFF한 경우 V2 리팩토링
     * - OSIV를 끄면 모든 지연로딩을 트랜잭션 안에서 처리해야 한다
     *  (영속성 컨텍스트의 생존 범위를 벗어난 `컨트롤러나 뷰에서 지연로딩이 불가함`)
     * - 아니면 레파지토리 계층에서 fetch join을 사용해야 한다
     */
    public List<OrderDto> ordersV2(OrderSearch orderSearch) {
        List<Order> orders = orderRepository.findAll(new OrderSearch());

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    /**
     * OSIV를 OFF한 경우 V1 리팩토링
     */
    public List<Order> ordersV1(OrderSearch orderSearch) {

        List<Order> all = orderRepository.findAll(orderSearch);

        //모든 지연 로딩을 트랜잭션 안에서 처리
        for (Order o : all) {
            o.getMember().getName(); //Lazy 강제 초기화, Member
            o.getDelivery().getAddress(); //Lazy 강제 초기화, Delivery
            List<OrderItem> orderItems = o.getOrderItems();
            orderItems.stream().forEach(oi -> oi.getItem().getName()); //Lazy 강제 초기화, Item
        }

        return all;
    }

}

/*
[ 커멘드와 쿼리 분리 ]

실무에서 OSIV를 끈 상태로 복잡성을 관리하는 좋은 방법이 있다. 바로 Command와 Query를 분리하는 것이다.
참고: https://en.wikipedia.org/wiki/Command–query_separation

보통 비즈니스 로직은 특정 엔티티 몇개를 등록하거나 수정하는 것이므로 성능이 크게 문제가 되지 않는다.
그런데 복잡한 화면을 출력하기 위한 쿼리는 화면에 맞추어 성능을 최적화 하는 것이 중요하다. 하지만 그 복잡성에 비해
핵심 비즈니스에 큰 영향을 주는 것은 아니다.
그래서 크고 복잡한 애플리케이션을 개발한다면, 이 둘의 관심사를 명확하게 분리하는 선택은 유지보수 관점에서 충분히 의미 있다.
단순하게 설명해서 다음처럼 분리하는 것이다.

* OrderService
    * OrderService: 핵심 비즈니스 로직
    * OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)

보통 서비스 계층에서 트랜잭션을 유지한다. 두 서비스 모두 트랜잭션을 유지하면서 지연 로딩을 사용할 수 있다.
 */