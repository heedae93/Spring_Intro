# 1. AOP란 무엇인가 + AOP 없이 Service 코드는 어떻게 망가지는가

## 1️⃣ AOP란 무엇인가? (가장 먼저 알아야 할 개념)

### AOP (Aspect-Oriented Programming)

AOP는  
**비즈니스 로직과 공통 관심사(cross-cutting concern)를 분리하기 위한 프로그래밍 패러다임**이다.

공통 관심사란 다음과 같은 것들이다.

- 실행 시간 측정
- 로그 출력
- 트랜잭션 처리
- 권한 체크
- 예외 로깅

이 로직들은:
- 모든 곳에서 필요하지만
- 비즈니스의 핵심 로직은 아니다

AOP의 목적은 다음 한 문장으로 요약된다.

> **비즈니스 코드에는 비즈니스 로직만 남기고  
> 공통 기능은 한 곳에서 관리하자**

---

## 2️⃣ AOP를 사용하지 않으면 어떤 문제가 생길까?

아래는 현재의 `MemberService` 코드다.
이 코드는 **AOP를 사용하지 않고, 비즈니스 로직만 깔끔하게 작성된 상태**다.

    @Service
    @Transactional
    public class MemberService {

        private final MemberRepository memberRepository;

        public MemberService(MemberRepository memberRepository) {
            this.memberRepository = memberRepository;
        }

        public Long join(Member member) {
            validateDuplicateMember(member);
            memberRepository.save(member);
            return member.getId();
        }

        public List<Member> findMembers() {
            return memberRepository.findAll();
        }

        public Optional<Member> findOne(Long memberId) {
            return memberRepository.findById(memberId);
        }
    }

이 상태에서  
**“모든 메서드의 실행 시간을 측정하라”**라는 요구사항이 생겼다고 가정해보자.

---

## 3️⃣ AOP 없이 실행 시간 측정을 추가하면 생기는 중복 코드

### join() 메서드

    public Long join(Member member) {
        long start = System.currentTimeMillis();
        try {
            validateDuplicateMember(member);
            memberRepository.save(member);
            return member.getId();
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("join time = " + (end - start));
        }
    }

---

### findMembers() 메서드

    public List<Member> findMembers() {
        long start = System.currentTimeMillis();
        try {
            return memberRepository.findAll();
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("findMembers time = " + (end - start));
        }
    }

---

### findOne() 메서드

    public Optional<Member> findOne(Long memberId) {
        long start = System.currentTimeMillis();
        try {
            return memberRepository.findById(memberId);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("findOne time = " + (end - start));
        }
    }

---

## 4️⃣ 이 방식의 문제점 (아주 중요)

### ① 중복 코드 폭발
- 모든 메서드에 동일한 코드 반복
- 메서드 수가 늘어날수록 유지보수 지옥

### ② 비즈니스 코드 가독성 붕괴
- “회원 가입”이 무엇을 하는지 한눈에 안 보임
- 핵심 로직이 부가 로직에 묻힘

### ③ 변경에 매우 취약
- 로그 형식 변경
- 시간 측정 방식 변경
  → **모든 메서드 수정 필요**

이 상태를 보고 하는 말이 바로 이것이다.

> “관심사가 섞였다”

---

## 5️⃣ AOP를 사용하면 무엇이 달라지는가?

AOP를 사용하면  
위에서 추가한 모든 코드가 **Service에서 완전히 제거된다.**

Service는 다시 원래 모습으로 돌아간다.

    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

그리고 실행 시간 측정은 **AOP 한 곳**에만 존재한다.

    @Aspect
    @Component
    public class TimeTraceAop {
        ...
    }

👉 중복 제거  
👉 책임 분리  
👉 유지보수성 폭발적 개선

---

## 6️⃣ Spring은 이 Service를 어떻게 “찾고” AOP를 적용하는가?

### ① @Service의 역할

    @Service

- 컴포넌트 스캔 대상
- Spring이 애플리케이션 시작 시 자동 탐색
- Bean으로 등록

즉,

    MemberService → Spring Bean

---

### ② AOP는 언제 적용되는가?

Spring은 다음 순서로 동작한다.

    1. MemberService 객체 생성
    2. 의존성 주입 완료
    3. BeanPostProcessor 실행
    4. AOP 조건에 맞으면 프록시 객체 생성
    5. 실제 Bean 대신 프록시를 컨테이너에 등록

결과적으로:

    Controller → 프록시 MemberService → 실제 MemberService

---

## 7️⃣ AOP는 Service에만 적용되는가?

❌ 아니다.

Pointcut 표현식에 따라 **Controller, Service, Repository 전부 적용 가능**하다.

예:

    execution(* com.spring.intro..*(..))

- Controller 메서드도 포함
- Service 메서드도 포함
- Repository 메서드도 포함

다만 실무에서는 보통:

- Service 중심으로 적용
- Controller는 로그용
- Repository는 최소화

---

## 8️⃣ @Transactional도 사실 AOP다

    @Transactional

이 어노테이션은:
- 컴파일 타임 기능 ❌
- 마법 ❌
- **Spring AOP 기반 프록시 동작 ⭕**

트랜잭션 시작 / 커밋 / 롤백
→ 전부 **AOP가 자동으로 끼어들어 처리**

---

## 9️⃣ 한 문장으로 정리 (핵심)

- AOP는 공통 관심사를 분리하기 위한 프로그래밍 기법이다.
- AOP가 없으면 Service 코드는 중복과 잡음으로 망가진다.
- Spring은 @Service로 Bean을 찾고, AOP 프록시를 씌운다.
- Controller, Service 모두 AOP 적용 대상이 될 수 있다.
- @Transactional 역시 AOP의 대표적인 예다.

이 때문에 AOP는  
**“Spring의 선택 기능이 아니라 핵심 기반 기술”**이다.



# 2. 왜 같은 클래스 내부 호출에는 AOP가 안 먹는가? / 왜 @Transactional은 public 메서드에만 붙어야 하는가?

이 두 문제의 원인은 하나다.

👉 **Spring AOP는 프록시 기반으로 동작한다**

---

## 1️⃣ Spring AOP의 전제 조건 (아주 중요)

Spring AOP는 다음 방식으로 동작한다.

- 실제 객체를 직접 건드리지 않는다
- 대신 **프록시 객체**를 하나 더 만들어서 감싼다

구조는 항상 이렇다.

    Client
      ↓
    Proxy (AOP 적용)
      ↓
    Target (실제 객체)

AOP는 **Proxy를 거칠 때만** 동작한다.

---

## 2️⃣ 왜 같은 클래스 내부 호출에는 AOP가 안 먹을까?

### 예제 코드

    @Service
    public class MemberService {

        public void outer() {
            inner();   // 같은 클래스 내부 호출
        }

        @Transactional
        public void inner() {
            // 트랜잭션 적용 기대
        }
    }

### 호출 흐름을 실제로 풀어보면

#### ❌ 우리가 기대하는 흐름

    Client
      → Proxy
          → inner()  (AOP 적용)

#### ✅ 실제 흐름

    Client
      → Proxy
          → outer()
              → this.inner()  ← 프록시 아님

핵심 포인트:

- `outer()`는 프록시를 통해 호출됨
- 하지만 `outer()` 안에서 호출한 `inner()`는
  **프록시를 거치지 않고 자기 자신(this)을 호출**

👉 **프록시를 통과하지 않았기 때문에 AOP 미적용**

---

## 3️⃣ 한 문장 요약 (내부 호출 문제)

> **Spring AOP는 프록시를 통과하는 호출에만 적용되며,  
> 같은 객체 내부에서의 메서드 호출은 프록시를 우회한다.**

그래서 내부 호출에는 AOP가 “안 먹는 것처럼” 보인다.

---

## 4️⃣ 그럼 @Transactional은 왜 public 메서드에만 붙어야 할까?

이것도 같은 이유다.

---

## 5️⃣ @Transactional의 실제 동작 방식

@Transactional은 다음과 같이 동작한다.

    Client
      → Transaction Proxy
          → begin transaction
              → target method 실행
          → commit / rollback

즉, **@Transactional = AOP Advice**다.

---

## 6️⃣ 왜 public이 아니면 안 되는가?

### Spring AOP의 기본 구현 방식

- JDK Dynamic Proxy (인터페이스 기반)
- 또는 CGLIB Proxy (클래스 상속 기반)

### 공통 제한 사항

- 외부에서 호출 가능한 메서드만 프록시가 가로챌 수 있음
- private / protected / package-private 메서드는
  **프록시 대상이 아님**

즉,

    private void txMethod() { }

이 메서드는:
- 외부에서 호출 불가
- 프록시가 끼어들 수 없음
- @Transactional 있어도 무시됨

---

## 7️⃣ public이어도 내부 호출이면 안 되는 이유

    @Transactional
    public void inner() { }

이 메서드가 public이어도,

    this.inner();

형태로 호출되면:

- 외부 호출 ❌
- 프록시 경유 ❌
- 트랜잭션 적용 ❌

즉,

> **@Transactional은 public + 외부 호출 + 프록시 경유**
> 이 세 조건이 모두 만족되어야 동작한다.

---

## 8️⃣ 내부 호출 문제의 정석적인 해결 방법

### 방법 1️⃣ 메서드 분리 (가장 정석)

    @Service
    public class OrderService {

        private final PaymentService paymentService;

        public void order() {
            paymentService.pay(); // 프록시 경유
        }
    }

    @Service
    public class PaymentService {

        @Transactional
        public void pay() {
            ...
        }
    }

👉 호출이 프록시를 통과 → 트랜잭션 정상 적용

---

### 방법 2️⃣ 구조적으로 책임 분리

- 트랜잭션 경계 = Service 단
- 내부 헬퍼 메서드에는 @Transactional 붙이지 않음

---

### ❌ 비추천 방법

- AopContext.currentProxy()
- 자기 자신 주입
- Lazy 트릭

👉 구조를 숨길 뿐, 좋은 설계가 아님

---

## 9️⃣ 두 질문을 하나로 묶은 최종 정리

### 왜 내부 호출에는 AOP가 안 먹는가?
- AOP는 프록시를 통해서만 동작한다
- 내부 호출은 프록시를 우회한다

### 왜 @Transactional은 public 메서드에만 붙어야 하는가?
- 프록시는 외부에서 접근 가능한 메서드만 가로챌 수 있다
- private / 내부 호출은 프록시 대상이 아니다

---

## 🔚 최종 한 문장 요약

> Spring AOP와 @Transactional은  
> **프록시를 통과하는 public 외부 호출에만 적용되며**,  
> 같은 클래스 내부 호출이나 private 메서드에는 적용되지 않는다.

이걸 이해했다면  
👉 Spring AOP 구조를 **제대로 이해한 단계**입니다.
