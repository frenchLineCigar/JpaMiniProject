package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by frenchline707@gmail.com on 2020-10-13
 * Blog : http://frenchline707.tistory.com
 * Github : http://github.com/frenchLineCigar
 */

@Embeddable //JPA 내장 타입
@Getter //값 타입은 변경 불가능하게 설계
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() { //protected
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
