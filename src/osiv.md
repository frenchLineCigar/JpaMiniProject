## API 개발 고급 > 실무 필수 최적화 > OSIV와 성능 최적화

이것을 모르면 장애로 이어질 수 있다.
트래픽이 조금만 많은 서비스에서는 OSIV에 대해서 명확하게 이해를 하고 넘어가야
장애가 났을때 대응할 수 있다 

- OSIV가 무엇인지?  
- OSIV를 사용하면 어떤 장단점이 있는지?  
- OSIV를 잘못 사용하면 어떤 장애가 발생하는지? 

<br>

### OSIV와 성능 최적화
* Open 'Session' In View: Hibernate
* Open 'EntityManager' In View: JPA  
(관례상 OSIV라 한다.)  
JPA의 EntityManager가 하이버네이트에선 Session


### OSIV ON

* ####`spring.jpa.open-in-view: true` 기본값

````
WARN 3496 --- [  restartedMain] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
````

이 기본값을 뿌리면서 애플리케이션 시작 시점에 warn 로그를 남기는 것은 이유가 있다.

*기본적으로 JPA가 언제 데이터베이스 커넥션을 가지고 오고, 언제 데이터베이스 커넥션을 DB에 반환 하는지 생각해보자.*

*언제 JPA가 데이터베이스 커넥션(Database Connection)을 가지고 올까?  
JPA라는 영속성 컨텍스트(Persistence Context)라는 요놈이 동작을 하려면 어쨌든
데이터베이스 커넥션을 내부적으로 사용을 해야 Lazy loading을 하든, 뭘 하든 작동한다.
그렇기 때문에 이 영속성 컨텍스트랑 데이터베이스 커넥션은 굉장히 밀접하게 매칭이 되어 있다.
JPA의 영속성 컨텍스트가 결국 데이터 베이스 커넥션을 1:1로 쓰면서 기능이 동작해야 된다.  
자 그런데, `그럼 언제 JPA가 데이터베이스 커넥션을 획득하냐면?` (진짜 디테일하게 들어가면 다를 수 있긴 한데)
기본적으로는 데이터베이스 트랜잭션(Database Transaction)을
시작할 때, JPA의 영속성 컨텍스트(Persistence Context)가 데이터베이스 커넥션(Database Connection, 이하 DBC)을 가져온다.
`그럼 DBC를 획득하면, 얘를 언제 DB에 돌려줘야 할까? 이 시점이 OSIV를 이해하는 것에서 중요한 부분이다!`  
DBC를 획득하는 시점은 어쨌든 트랜잭션을 시작할 때가 제일 처음이다. 서비스 계층에서 트랜잭션(@Transactional) 시작하는 시점에
DBC를 가지고 온다. 이를 돌려주는 시점은 OSIV의 사용 여부에 따라 차이가 난다.*
 
*OSIV라는 것이 켜져 있으면(default가 true) : 서비스 계층에서 @Transactional 이 끝나고 컨트롤러 단으로 나가더라도 DBC를 반환 하지 않는다.*

`왜 반환을 하지 않을까?` <장점>

OSIV 전략은 트랜잭션 시작처럼 최초 데이터베이스 커넥션 시작 시점부터 API `응답(Response)이 끝날 때 까지` 영속성
컨텍스트와 데이터베이스 커넥션을 유지한다. 
그래서 지금까지 `View Template이나 API 컨트롤러에서 지연 로딩이 가능했던 것`이다.  
`지연 로딩`은 영속성 컨텍스트가 살아있어야 가능하고, `영속성 컨텍스트`는 기본적으로 데이터베이스 커넥션
을 유지한다. 이것 자체가 큰 장점이다.  

`이 전략의 치명적인 단점은?`  
`커넥션이 말라버린다...` <트레이드 오프가 있음>

그런데 이 전략은 `너무 오랜시간동안 데이터베이스 커넥션 리소스를 사용`하기 때문에,
`실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 모자랄 수 있다.` 이것은 `결국 장애로 이어진다.`

*생각해보면 일반적인 애플리케이션에서는 DBC가 끝나면 그냥 커넥션 반환하면 되잖슴?
근데 얘는 DBC를 그냥 끝까지 들고 있다가, 진짜 고객한테 Response 주는 타이밍이 되서야 커넥션을 반환한단 말임.*

`단적인 예`

예를 들어서 컨트롤러에서 외부 API를 호출하면 외부 API 대기 시간 만큼 커넥션 리소스를 반환하지 못하
고, 유지해야 한다.

*외부 API가 한 3초 걸린다고 하면, DBC도 그때동안 반환 못하고 있는 것이다. 그때동안 고객한테 응답을 준 것이 아니기 때문에.
만약 외부 API가 블로킹(Blocking)이라도 걸리면, 쓰레드 풀(Thread Pool)이 다 차버릴 때까지 DBC를 다 먹어버림. 
한마디로 DBC를 너무 오래 물고 있는다는 점이 OSIV(Open Session In View) 전략의 `치명적인 단점`이다.*

*장점은 엔티티(Entity)의 지연 로딩(Lazy loading)같은 기술을 `컨트롤러나 VIEW 단에서 적극 활용할 수 있다`는 점이
 개발 입장에서는 중복도 줄이고, 유지보수 성을 높이는 장점이 있다.*


### OSIV OFF


* ####`spring.jpa.open-in-view: false` OSIV 종료

OSIV를 끄면 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환한다.
따라서 `커넥션 리소스를 낭비하지 않는다.`  
`OSIV를 끄면 모든 지연로딩을 트랜잭션 안`에서 처리해야 한다.
(영속성 컨텍스트의 생존 범위를 벗어난 `컨트롤러나 뷰에서 지연로딩이 불가함`)

````
//OSIV를 OFF한 상태로, 컨트롤러에서 Lazy 강제 초기화 시키는 경우 LazyInitializationException 발생
"message": "could not initialize proxy [jpabook.jpashop.domain.Member#1] - no Session"
"trace": "org.hibernate.LazyInitializationException: could not initialize proxy [jpabook.jpashop.domain.Member#1] - no Session
````
*프록시 초기화 불가능해서 지연 로딩 불가하다는 예외가 터짐 -> OSIV를 껐기 때문에 컨트롤러에 더 이상 영속성 컨텍스트가 존재하지 않으므로.
컨트롤러, 뷰 등의 웹 계층에 지연 로딩을 강제하는 코드를 더이상 둘 수 없다.*

따라서 지금까지 작성한 많은 `지연 로딩 코드`를 `트랜잭션 안`으로 넣어야 하는 단점이 있다. 
그리고 `view template`에서 지연로딩이 동작하지 않는다.
결론적으로 `트랜잭션이 끝나기 전에 지연 로딩을 강제로 호출해 두어야` 한다 (레포지토리 계층에서 아싸리 `fetch join`을 사용하거나, 서비스 계층에서 강제 lazy 로딩을 시켜야 함)

*OSIV OFF를 하게 되면 트랜잭션을 시작하고 끝날 때 까지만 딱 DBC를 유지하고, 영속성 컨텍스트도 딱 트랜잭션 범위까지만 유지가 된다.
 DBC가 딱 끝나면 영속성 컨텍스트도 날라가고 DBC도 반환을 해줌.  
 그렇기 때문에 장점은 DBC를 굉장히 짧은 기간동안 유지하는 것이다.*  
 
 *고객이 요청이 왔다?([GET]/api/v1/members -> MemberApiController.saveMemberV1) 
 -> 트랜잭션에서 원하는 로직을 돌림 (@Transaction MemberService.join) 이때 영속성 컨텍스트도 만들어지고, DBC도 가지고 왔다가 이 로직이 끝나면 플러시 커밋 다 치고 영속성 컨텍스트를 날려버리고 깔끔하게 DBC을 반환 한다
 -> 그래서 join에서 saveMemberV1로 반환한 시점부터는 영속성 컨텍스트도 DBC도 안쓴다
 -> 그렇기 때문에 그 밑에 어떠한 로직이 있어도(외부 API를 호출하더라도 더이상 이전의 커넥션을 물고있지 않는다) DBC을 DB의 커넥션 풀(Connection Pool)에 반환해버렸기 때문에.
 -> 그래서 트래픽이 많거나 실시간 사용자의 요청이 많은 경우에는 커넥션 리소스를 훨씬 더 유연하게 쓸 수 있는 장점이 있다. 트랜잭션이 끝나면 바로 바로 반환하므로 수많은 요청에 따른 커넥션을 원활하게 조달할 수 있으니까!*
 

### 커멘드와 쿼리 분리
실무에서 OSIV를 끈 상태로 복잡성을 관리하는 좋은 방법이 있다. 바로 Command와 Query를 분리하는 것이다.  
참고: https://en.wikipedia.org/wiki/Command–query_separation

보통 비즈니스 로직은 특정 엔티티 몇개를 등록하거나 수정하는 것이므로 성능이 크게 문제가 되지 않는다.
그런데 복잡한 화면을 출력하기 위한 쿼리는 화면에 맞추어 성능을 최적화 하는 것이 중요하다. 하지만 그 복잡성에 비해
핵심 비즈니스에 큰 영향을 주는 것은 아니다.  
그래서 크고 복잡한 애플리케이션을 개발한다면, 이 둘의 관심사를 명확하게 분리하는 선택은 유지보수 관점에서 충분히 의미 있다.  
단순하게 설명해서 다음처럼 분리하는 것이다.

* OrderService
    * OrderService: 핵심 비즈니스 로직
    * OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
    
보통 서비스 계층에서 트랜잭션을 유지한다. 두 서비스 모두 트랜잭션을 유지하면서 지연 로딩을 사용할 수 있다.

> 추천: 고객 서비스의 실시간 API는 OSIV를 끄고, ADMIN 처럼 커넥션을 많이 사용하지 않는 곳에서는 OSIV를 켠다.

> 참고: OSIV에 관해 더 깊이 알고 싶으면 자바 ORM 표준 JPA 프로그래밍 13장 웹 애플리케이션과 영속성 관리를 참고



OrderQueryService 처럼
API나 화면 처리를 위한 쿼리용 서비스를 별도로 분리하자  
-> 애플리케이션이 커지면 아키텍쳐 상 패키지 구조를 핵심 서비스 비즈니스랑 단순 API 쿼리용으로 분리하는 게 좋다. 

도메인을 분리 시키고 별도로 쿼리용 패키지를 구성하면 더 좋다

EX)  
**member**  
`member.controller.*`  
`member.service.*`  
`member.service.query` -> 이런 식으로 쿼리용 서비스를 별도로 분리하는 패키지를 둔다.  
`member.repository.*`   
