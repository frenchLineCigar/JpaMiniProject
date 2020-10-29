package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

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

        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행) : 루프를 돌며 각각 orderItems를 넣어준다
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); //Query N번 (2번 실행)
            o.setOrderItems(orderItems);
        });

        return result;
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
