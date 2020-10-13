package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by frenchline707@gmail.com on 2020-10-13
 * Blog : http://frenchline707.tistory.com
 * Github : http://github.com/frenchLineCigar
 */

@Entity
@DiscriminatorValue("M") //값을 명시하지 않으면 클래스명이 기본값
@Getter
@Setter
public class Movie extends Item {

    private String director;
    private String actor;
}
