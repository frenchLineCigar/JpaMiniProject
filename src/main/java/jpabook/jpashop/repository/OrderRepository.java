package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
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


}
