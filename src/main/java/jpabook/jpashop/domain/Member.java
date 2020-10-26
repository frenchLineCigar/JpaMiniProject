package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 엔티티
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "name_unique", columnNames = "name")})
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") //일대다, 하나의 회원이 여러 개의 상품을 주문할 수 있음, 읽기 전용(Read-only) 필드
    private List<Order> orders = new ArrayList<>(); //컬렉션은 필드에서 바로 초기화하는 것이 안전 : null 문제 & 하이버네이트는 엔티티를 영속화 할 때, 컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경

}
