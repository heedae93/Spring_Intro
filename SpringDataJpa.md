# JPA 직접 사용 → Spring Data JPA 전환 설명

## 1️⃣ 구조 변화의 핵심 요약

기존 구조는 **JPA(EntityManager)를 직접 사용하여 Repository 구현 클래스를 작성**했다.

전환 후 구조는 **Spring Data JPA가 Repository 구현을 자동으로 생성**하고,
개발자는 **인터페이스만 정의**한다.

즉,

- 기존: JPA 표준 API를 직접 사용
- 현재: JPA 위에 Spring Data JPA 추상화를 사용

---

## 2️⃣ 기존 JPA 방식의 특징 (EntityManager 직접 사용)

기존에는 다음과 같은 Repository 구현 클래스가 필요했다.

    public class JpaMemberRepository implements MemberRepository {

        private final EntityManager em;

        public JpaMemberRepository(EntityManager em) {
            this.em = em;
        }

        public Member save(Member member) {
            em.persist(member);
            return member;
        }

        public Optional<Member> findById(Long id) {
            return Optional.ofNullable(em.find(Member.class, id));
        }

        public List<Member> findAll() {
            return em.createQuery("select m from Member m", Member.class)
                     .getResultList();
        }

        public Optional<Member> findByName(String name) {
            return em.createQuery(
                    "select m from Member m where m.name = :name",
                    Member.class
                )
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findAny();
        }
    }

### 이 방식의 특징

- JPA(EntityManager)를 직접 사용
- CRUD 로직을 개발자가 직접 구현
- ORM의 동작 원리를 이해하는 데 매우 좋음
- 하지만 반복 코드가 많아짐

즉, **JPA는 ORM 표준일 뿐, CRUD를 자동으로 제공하지 않는다.**

---

## 3️⃣ Spring Data JPA 방식의 핵심 변화

Spring Data JPA를 사용하면,
Repository 구현 클래스를 **직접 작성하지 않는다.**

아래 인터페이스 선언만으로 충분하다.

스프링 데이터 JPA가 `SpringDataJpaMemberRepository` 를 스프링 빈으로 자동 등록해준다.

    public interface SpringDataJpaMemberRepository
            extends JpaRepository<Member, Long>, MemberRepository {

        Optional<Member> findByName(String name);
    }

### 여기서 중요한 점

- `JpaRepository`를 상속하는 순간
    - save
    - findById
    - findAll
    - delete
      등의 CRUD 메서드가 자동 제공된다.

- 개발자는 **구현 클래스 작성 ❌**
- Spring이 런타임에 **프록시 구현체를 자동 생성 ⭕**

---

## 4️⃣ Spring Data JPA에서 CRUD는 어떻게 동작하는가?

Spring Data JPA 내부 구조는 다음과 같다.

    Service
      ↓
    Spring Data JPA Repository (프록시)
      ↓
    EntityManager (JPA)
      ↓
    Hibernate
      ↓
    JDBC
      ↓
    Database

즉,

- Spring Data JPA는 JPA를 대체하지 않는다.
- JPA(EntityManager)를 **더 쉽게 사용하도록 감싸는 계층**이다.

---

## 5️⃣ 메서드 이름으로 쿼리를 만드는 이유

다음 메서드를 보면:

    Optional<Member> findByName(String name);

Spring Data JPA는 이 메서드 이름을 분석해서
다음 의미로 해석한다.

    select m
    from Member m
    where m.name = :name

즉,

- SQL ❌
- JPQL ❌
- **메서드 이름 규칙 기반 쿼리 생성 ⭕**

필요하면 @Query로 직접 JPQL을 작성할 수도 있다.

---

## 6️⃣ SpringConfig에서의 변화 설명

현재 설정 클래스는 다음과 같다.

    @Configuration
    public class SpringConfig {

        private final MemberRepository memberRepository;

        public SpringConfig(MemberRepository memberRepository) {
            this.memberRepository = memberRepository;
        }

        @Bean
        public MemberService memberService() {
            return new MemberService(memberRepository);
        }
    }

### 여기서 중요한 점

- `MemberRepository` 타입만 의존
- 구현체를 직접 new 하지 않음
- Spring이 자동으로 주입

Spring Data JPA가 활성화되어 있으면,
`SpringDataJpaMemberRepository`의 구현체가 자동 생성되고
`MemberRepository` 타입으로 주입된다.

---

## 7️⃣ 왜 memberRepository() Bean 메서드가 사라졌는가?

기존에는 구현체를 직접 Bean으로 등록했다.

    @Bean
    public MemberRepository memberRepository() {
        return new JpaMemberRepository(em);
    }

Spring Data JPA 사용 시에는 이 과정이 불필요하다.

- Repository 인터페이스를 정의하면
- Spring이 자동으로 Bean 등록
- 자동 프록시 생성

따라서 `@Bean memberRepository()` 메서드는 제거된다.

---

## 8️⃣ JPA vs Spring Data JPA 역할 정리 (중요)

### JPA
- ORM 표준 API
- 객체와 테이블 매핑이 목적
- CRUD 자동 생성 ❌
- EntityManager 기반

### Spring Data JPA
- JPA 사용을 편하게 만드는 Spring 추상화
- Repository 구현 자동 생성
- CRUD 메서드 자동 제공 ⭕

즉,

> JPA는 “ORM의 규칙”이고  
> Spring Data JPA는 “그 규칙을 쓰기 쉽게 만든 도구”다.

---

## 9️⃣ 최종 요약

- 기존 방식은 JPA(EntityManager)를 직접 사용하여 CRUD를 구현했다.
- Spring Data JPA를 도입하면 Repository 구현이 자동화된다.
- 개발자는 인터페이스만 정의하면 된다.
- ORM의 본질(JPA)은 그대로 유지되며,
  생산성과 가독성만 크게 개선된다.

이 전환은
**“ORM 개념을 버린 것”이 아니라,
“ORM을 가장 실무적으로 사용하는 방식으로 이동한 것”이다.**
