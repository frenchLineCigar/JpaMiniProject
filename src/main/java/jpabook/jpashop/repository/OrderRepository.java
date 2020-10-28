package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QDelivery;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 레파지토리
 */
@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }


    /* 주문 조회 : findAll(OrderSearch orderSearch) 메서드는 검색 조건에 동적으로 쿼리를 생성해서 주문 엔티티를 조회 */

    /**
     * 동적 쿼리 생성 : QueryDSL로 처리
     * - 실무에서는 조건에 따라서 실행되는 쿼리가 달라지는 동적 쿼리를 많이 사용한다
     * - 굉장히 직관적이다
     * - 오타를 컴파일 시점에 다 잡아준다
     */
    public List<Order> findAll(OrderSearch orderSearch) {

        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;

        return query.select(order)
                .from(order)
                .join(order.member, member)
                .where(
                        statusEq(orderSearch.getOrderStatus()),
                        nameLike(orderSearch.getMemberName())
                ) // Skips null arguments
                .limit(1000)
                .fetch();
    }

    private BooleanExpression idEq(Long idCond) {
        if (idCond == null) {
            return null;
        }
        return QOrder.order.id.eq(idCond);
    }
    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }
    private BooleanExpression nameLike(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return null;
        }
        return QMember.member.name.like(memberName);
    }

    /**
     * JPQL로 동적 쿼리 처리
     * - 실무에서 못씀
     * - 존나 무식하게 쌩짜로 짜는 방법
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {

        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);//최대 1000건

        //파라미터 바인딩
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     * JPA Criteria 로 동적 쿼리 처리
     *  - 실무에서 못씀
     *  - 역시 쓰기도 어렵고, 직관적으로 무슨 SQL이 만들어지는지 바로 감이 안오고, 유지 보수성이 거의 제로에 가깝다
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class); //무슨 쿼리가 생성될 지 머리에 떠오르냐?
        Join<Object, Object> m = o.join("member", JoinType.INNER); //회원과 조인

        List<Predicate> criteria = new ArrayList<>(); //동적 쿼리에 대한 컨디션 조합을 Predicate를 깔끔하게 만들 수 있음

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    /**
     * 주문(ORDER) + 배송정보(DELIVERY) + 회원(MEMBER)을 조회
     * * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
     * * 페치 조인으로 order -> member , order -> delivery 는 이미 조회 된 상태 이므로 지연로딩 자체가 일어나지 X
     * * 재사용성 증대
     */
    public List<Order> findAllWithMemberDelivery() { //ORDER를 가져올때 MEMBER, DELIVERY까지 객체 그래프를 한방에 가져오도록 쿼리
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }
    /**
     * findAllWithMemberDelivery()에 QueryDSL 적용
     */
    public List<Order> findAllWithMemberDeliveryByQueryDSL(OrderSearch orderSearch) { //ORDER를 가져올때 MEMBER, DELIVERY까지 객체 그래프를 한방에 가져오도록 쿼리
        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QDelivery delivery = QDelivery.delivery;

        return query.select(order)
                .from(order)
                .join(order.member, member)
                .fetchJoin() //이렇게 페치 조인
                .join(order.delivery, delivery)
                .fetchJoin()
                .where(
                        statusEq(orderSearch.getOrderStatus()),
                        nameLike(orderSearch.getMemberName())
                ) // Skips null arguments
                .limit(1000)
                .fetch();
    }

    /**
     * 주문(ORDER) + 배송(DELIVERY) + 회원(MEMBER) + 주문 상품(ORDERITEM) + 상품(ITEM)을 조회
     *
     * 단점 : 페이징 불가능
     * > 컬렉션 페치 조인을 사용하면 페이징이 불가능하다.
     * > 일대다(1 : N)를 페치 조인(fetch join)하는 순간, 페이징 관련 쿼리는 아예 안 나간다.
     * > 경고 로그를 남기며 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징(매우 위험함)
     * > WARN o.h.h.internal.ast.QueryTranslatorImpl   : HHH000104: firstResult/maxResults specified with `collection fetch`; applying in memory!
     * > 컬렉션 페치 조인(일대다 페치 조인)은 1개만 사용할 수 있다. 컬렉션이 둘 이상에 페치 조인을 사용하면 안된다. 데이터가 부정합하게 조회될 수 있다.
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" + //distinct -> 엔티티 식별자가 같은 경우에 중복을 걸러서 컬렉션에 담아준다.
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" + //one to MANY -> collection fetch join
                        " join fetch oi.item i", Order.class)
                //.setFirstResult(1) //첫번째 결과 row로 가져올 시작 위치, 0부터 시작 -> offset 쿼리 안나감
                //.setMaxResults(100) //가져올 row 수 -> limit 쿼리 안나감
                .getResultList();
    }
}
