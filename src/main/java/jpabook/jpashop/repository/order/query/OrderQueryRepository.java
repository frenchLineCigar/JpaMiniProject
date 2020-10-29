package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 특정 화면, API와 의존 관계가 fit한 조회 쿼리 전용
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번
     * 단건 조회에서 많이 사용하는 방식
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        //루트 조회(toOne 코드를 모두 한번에 조회) : N에 해당하는 컬렉션 필드를 제외한 order를 담는다(collection인 orderItems는 못넣음)
        List<OrderQueryDto> result = findOrders(); //Query 1번 -> N개 (결과 2개)

        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행) : 루프를 돌며 각각 orderItems를 넣어준다 -> 루프를 돌 때 마다 쿼리를 날림
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); //Query N번 (2번 실행)
            o.setOrderItems(orderItems);
        });

        return result;
    }

    /**
     * 최적화
     * Query: 루트 1번, 컬렉션 1번
     * 데이터를 한꺼번에 처리할 때 많이 사용하는 방식
     *
     */
    public List<OrderQueryDto> findAllByDto_optimization() {

        //루트 조회(toOne 코드를 모두 한번에 조회)
        //주문을 다 가져온다 (컬렉션 필드 제외)
        List<OrderQueryDto> result = findOrders();

        //스트림을 돌려 orderId 리스트를 추출
        List<Long> orderIds = toOrderIds(result);

        //orderItem 컬렉션을 MAP 한방에 조회
        //현재 주문들와 관련된 주문상품을 IN 쿼리를 사용해 한방에 다 조회해 Map으로 가공한다 (매칭 성능 향상 및 추출하기 편하게 Map으로 가공)
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행X)
        //메모리에 올려둔 Map에서 찾아서 추출
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

//        [ Map 으로 가공 안하면 ? ] 스트림을 이용해 orderItems 를 그대로 사용해 값 셋팅 해보기
//        result.stream().forEach(o -> {
//            List<OrderItemQueryDto> orderItemList = orderItems.stream()
//                    .filter(orderItem -> o.getOrderId().equals(orderItem.getOrderId()))
//                    .collect(Collectors.toList());
//            o.setOrderItems(orderItemList);
//        });

        return result;

    }

    /**
     * 1:N 관계인 orderItems 조회(IN 쿼리 사용해 한방 조회)후 Map으로 가공
     */
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        //쿼리 한번으로 현재 주문들와 관련된 주문상품을 한방에 다 조회한다(IN 쿼리 사용) -> 쿼리를 1번 날리고 다 가져온 다음에 메모리에서 매칭시켜 값을 셋팅한다
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" + //(OrderItem 입장에서 Item은) ToOne 관계이므로 조인해도 데이터 row 수가 증가하지 않는다
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        //매핑을 위한 코드도 작성하기 쉽고 성능도 더 최적화 할 수 있게 Map으로 바꾼다
        return orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId())); //-> 메모리의 Map에 올려둔다
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }

    /**
     * 1:N 관계(컬렉션)를 제외한 나머지를 한번에 조회
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                //new operation으로 jpql을 짜더라도 collection(orderItems)을 바로 넣을 수 없음, sql은 data를 flat하게 한줄밖에 못넣음
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" + //(Order 입장에서 Member는) ToOne 관계이므로 join해도 데이터 row 수가 증가하지 않는다
                        " join o.delivery d", OrderQueryDto.class) //(Order 입장에서 Delivery은) ToOne 관계이므로 join해도 데이터 row 수가 증가하지 않는다
                .getResultList();
    }

    /**
     * 1:N 관계인 orderItems 조회
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" + //(OrderItem 입장에서 Item은) ToOne 관계이므로 조인해도 데이터 row 수가 증가하지 않는다
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
