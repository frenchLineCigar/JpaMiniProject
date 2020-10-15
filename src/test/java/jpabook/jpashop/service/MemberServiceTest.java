package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * 회원 기능 테스트
 */
@RunWith(SpringRunner.class) //JUnit4 실행 시 스프링과 같이 실행
@SpringBootTest //스프링 부트를 띄운 상태로 테스트(스프링 컨테이너 안에서 테스트)
@Transactional //테스트 수행 후, DB 롤백 (= 기본적으로 영속성 컨텍스트를 flush 하지 않음)
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("lucy");

        //when
        Long savedId = memberService.join(member);

        //then
        assertEquals(member, memberRepository.findOne(savedId)); //true -> why? : JPA에서 같은 트랜잭션 안에서 같은 Entity(아이디 값(PK)이 똑같은 Entity)면 같은 영속성 컨텍스트에서 하나의 똑같은 객체로 관리
    }
    
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("lucy");

        Member member2 = new Member();
       member2.setName("lucy");

        //when
        memberService.join(member1);
        memberService.join(member2); //예외가 발생해야 한다!!!

        //then
        fail("예외가 발생해야 한다."); //fail(): 코드가 여기까지 도달하면 안됨, 여기까지 도달 시 뭔가 잘못 됨을 알리며 테스트 실패 뜸
    }
    
    @Test
    public void 회원_단건_조회() throws Exception {
        //given
        Member member = new Member();
        member.setName("lucy");
        Long savedId = memberService.join(member);

        //when
        Member findOne = memberService.findOne(savedId);
        Member findOneDirectly = memberRepository.findOne(savedId);

        //then
        System.out.println("member = " + member);
        System.out.println("findOne = " + findOne);
        System.out.println("findOneDirectly = " + findOneDirectly);
        assertEquals(member, findOne);
        assertEquals(member, findOneDirectly);
        assertEquals(findOne, findOneDirectly);
    }
    
    @Test
    public void 이름으로_회원조회() throws Exception {
        //given
        Member member = new Member();
        member.setName("lucy");
        memberService.join(member);

        //when
        List<Member> findByName = memberService.findByName("lucy");
        List<Member> direct = memberRepository.findByName("lucy");

        //then
        assertSame(findByName.get(0), member); // == 비교
        assertSame(direct.get(0), member);
//        assertEquals(findByName.get(0), member);
    }
    
    @Test
    public void 회원전체조회() throws Exception {
        //given
        List<Member> memberList = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            Member member = new Member();
            member.setName("member" + i);
            memberList.add(member);
        }
        for (Member member : memberList) {
            memberService.join(member);
        }

        //when
        List<Member> members = memberService.findMembers();
        List<Member> direct = memberRepository.findAll();
        //then
        assertEquals(memberList.size(), members.size());
        assertEquals(memberList.size(), direct.size());
    }


}