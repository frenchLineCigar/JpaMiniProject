package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 상품 레파지토리
 */

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    /**
     * 상품 등록
     */
    public void save(Item item) {
        if (item.getId() == null) { //아이디가 없으면 새로운 객체로 보고 persist() 호출
            em.persist(item); //신규 등록 - Make an instance managed and persistent.
        } else { //아이디가 있으면 merge() 호출
            Item merge = em.merge(item);//리턴받은 merge만 영속성 객체
        }
    }
    /**
     * 준영속 엔티티 수정 방법 2: 병합( merge ) 사용
     * 주의: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이
     * 변경된다. 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)
     * 실무에서는 보통 업데이트 기능이 매우 재한적이다. 그런데 병합은 모든 필드를 변경해버리고, 데이터
     * 가 없으면 null 로 업데이트 해버린다.
     * -> 따라서 실무에서는 거의 쓰지 x, 단순한 경우에만 사용
     */

    public Item findOne(Long id) {
        return em.find(Item.class,id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

    //추가 해본 것
    public List<Item> findByName(String name) {
        return em.createQuery("select i from Item i where i.name =: name",Item.class)
                .setParameter("name", name)
                .getResultList();
    }

}
