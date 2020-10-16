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
        if (item.getId() == null) { //아이디가 없으면 새걸로 보고 persist() 호출
            em.persist(item); //신규 등록 - Make an instance managed and persistent.
        } else { //아이디가 있으면 merge() 호출
            em.merge(item); //Merge the state of the given entity into the current persistence context.
        }
    }

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
