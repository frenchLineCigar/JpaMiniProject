package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by frenchline707@gmail.com on 2020-10-13
 * Blog : http://frenchline707.tistory.com
 * Github : http://github.com/frenchLineCigar
 */

@RunWith(SpringRunner.class) //junit에게 스프링과 관련된 테스트임을 알림
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    
    @Test
    @Transactional //EntityManager를 통한 모든 데이터 변경은 항상 트랜잭션 안에서 이뤄져야 한다
    @Rollback(false) //or @Commit
    public void testMember() throws Exception {
        //given
        Member member = new Member();
        member.setUsername("memberA");

        //when
        Long saveId = memberRepository.save(member);
        Member findMember = memberRepository.find(saveId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());


        Assertions.assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장 (findMember == member)
        System.out.println("findMember == member: " + (findMember == member));
        /*
        @ 영속성 컨텍스트에서 식별자가 같으면 같은 엔티티로 인식한다 (1차 캐쉬에서 꺼내옴)
        : 같은 트랜잭션 안에서 저장을 하고 조회를 하면 영속성 컨텍스트가 똑같다.
        같은 영속성 컨텍스트 안에서는 아이디(식별자) 값이 같으면 같은 엔티티로 식별함
         */
    }
}