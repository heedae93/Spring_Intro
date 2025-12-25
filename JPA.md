# JpaMemberRepository 코드 설명

## 1️⃣ 이 클래스의 정체

`JpaMemberRepository`는 **JPA(EntityManager)를 직접 사용해서**
`Member` 엔티티를 영속화·조회하는 **Repository 구현체**이다.

중요한 점은 이 클래스는 **Spring Data JPA가 아니라, 순수 JPA를 사용한 Repository 구현**이라는 것이다.

- Spring Data JPA ❌
- 순수 JPA(EntityManager) ⭕
- ORM의 본질을 보여주는 코드

즉, **JPA는 CRUD를 자동으로 만들어주지 않기 때문에**
개발자가 Repository 구현 클래스에서 **CRUD 동작을 직접 작성**해야 한다.

---

## 2️⃣ JPA와 Spring Data JPA의 역할 차이 (중요)

이 코드를 이해하려면 먼저 다음 개념을 정확히 구분해야 한다.

### JPA
- ORM(Object-Relational Mapping) **표준 API**
- 객체와 테이블을 매핑하는 것이 목적
- CRUD 자동 생성 ❌
- `EntityManager`를 통해 **개발자가 직접 CRUD 동작을 호출**해야 함

### Spring Data JPA
- JPA를 더 쉽게 쓰기 위한 **Spring의 추상화 라이브러리**
- Repository 구현을 자동으로 생성
- CRUD 메서드(save, findById 등)를 자동 제공 ⭕

즉,

- **JPA의 목적은 CRUD 제거가 아니라 “객체-관계 매핑”**
- **CRUD 자동화는 Spring Data JPA의 역할**

이 `JpaMemberRepository`는
> “Spring Data JPA 없이, JPA만 사용하면 Repository를 이렇게 직접 구현해야 한다”
는 것을 보여주는 예제다.

---

## 3️⃣ 핵심 구성요소

### EntityManager

    private final EntityManager em;

EntityManager는 **JPA의 핵심 인터페이스**로,
엔티티의 생명주기(저장, 조회, 수정, 삭제)를 관리한다.

개념적으로는 JDBC의 다음 요소들을 모두 포함하는 상위 개념이다.

- Connection
- Statement
- ResultSet
- 1차 캐시
- 엔티티 상태 관리

즉, JDBC에서 개발자가 직접 처리하던 것들을
JPA에서는 EntityManager가 대신 관리한다.

---

### 생성자 주입

    public JpaMemberRepository(EntityManager em) {
        this.em = em;
    }

- Spring이 JPA 구현체(Hibernate)가 만든 EntityManager를 주입한다.
- Repository는 JPA 표준 인터페이스(EntityManager)에만 의존한다.
- Hibernate 같은 구현체는 숨겨져 있다.

---

## 4️⃣ 메서드별 동작 설명 (CRUD를 직접 구현)

### 🔹 save() – CREATE

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

- JPA에서는 INSERT SQL을 직접 작성하지 않는다.
- 대신 `persist()`를 호출해 엔티티를 **영속성 컨텍스트에 등록**한다.
- 실제 INSERT SQL은 트랜잭션 커밋 시점에 실행된다.

즉, JPA에서의 CREATE는
> SQL 작성이 아니라 **객체 상태를 영속 상태로 만드는 것**이다.

---

### 🔹 findById() – READ (단건)

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

- PK 기반 조회
- 1차 캐시(영속성 컨텍스트)를 먼저 확인
- 캐시에 없을 경우에만 DB 조회

개념적으로 실행되는 SQL:

    select * from member where id = ?

하지만 이 SQL은 개발자가 작성하지 않는다.

---

### 🔹 findAll() – READ (전체)

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

이 쿼리는 SQL이 아니라 **JPQL(Java Persistence Query Language)** 이다.

- 테이블 기준 ❌
- 엔티티 기준 ⭕
- 컬럼 기준 ❌
- 필드 기준 ⭕

  select m from Member m

의미:
Member 엔티티 전체를 조회하라

Hibernate가 이를 실제 SQL로 변환한다.

---

### 🔹 findByName() – READ (조건)

    public Optional<Member> findByName(String name) {
        List<Member> result = em.createQuery(
            "select m from Member m where m.name = :name",
            Member.class
        )
        .setParameter("name", name)
        .getResultList();

        return result.stream().findAny();
    }

- 엔티티와 필드 기준 조건 조회
- 파라미터 바인딩 사용
- SQL Injection 방지
- 내부적으로 PreparedStatement 방식 사용

---

## 5️⃣ JPA에서 UPDATE와 DELETE는 왜 메서드가 없나?

JPA에서는 UPDATE SQL을 직접 호출하지 않는다.

    member.setName("newName");

- 엔티티 상태 변경
- 트랜잭션 커밋 시 변경 감지(Dirty Checking)
- UPDATE SQL 자동 생성

DELETE 역시 마찬가지다.

    em.remove(member);

즉, JPA의 CRUD는
> SQL을 직접 호출하는 방식이 아니라  
> **엔티티 상태를 변경하는 방식**이다.

---

## 6️⃣ ORM 관점에서 이 코드의 의미

이 Repository에서 개발자가 신경 쓰는 것

- 객체 (Member)
- 필드 (name)
- 객체 상태

신경 쓰지 않는 것

- SQL 문법
- 테이블 구조
- DB 방언
- JDBC 자원 관리

이것이 ORM(Object-Relational Mapping)의 핵심 가치다.

---

## 7️⃣ 최종 요약 (핵심 정리)

- JPA는 CRUD를 자동 생성하기 위해 만들어진 기술이 아니다.
- JPA의 목적은 **객체와 관계형 DB 사이의 패러다임 차이를 해결하는 것**이다.
- 따라서 JPA만 사용할 경우, Repository 구현과 CRUD 호출은 개발자가 직접 작성해야 한다.
- CRUD 메서드를 자동으로 제공하는 것은 **Spring Data JPA의 역할**이다.

이 `JpaMemberRepository`는  
**“JPA의 본질과 Spring Data JPA의 차이”를 가장 잘 보여주는 예제 코드**다.
