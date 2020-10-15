package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 회원 레파지토리
 */

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    /* 회원 저장 */
    public void save(Member member) {
        em.persist(member); //영속성 컨텍스트에 member 객체를 넣고, 트랜잭션이 commit 되는 시점에 DB에 반영(insert)
    }

    /* 회원 조회 (단건) */
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    } // 단건 조회, find(1st: 조회 타입, 2nd: PK)

    /* 회원 조회 (목록) */
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class) // createQuery(1st: JPQL, 2nd: 반환 타입 ), JPQL -> from의 대상인 Member가 테이블이 아니라 엔티티임
                .getResultList();
    }

    /* 특정 이름의 회원 조회 */
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name) //파라미터 바인딩
                .getResultList();
    }

}
