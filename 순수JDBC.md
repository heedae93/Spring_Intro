# JDBC 리포지토리 구현과 스프링 설정(SpringConfig)을 통한 OCP 준수

## 1. 순수 JDBC(Java Database Connectivity) 개요
`JdbcMemberRepository`는 자바의 표준 데이터베이스 연결 기술인 JDBC를 직접 사용하여 구현되었습니다.

### 주요 특징 및 흐름
* **직접적인 SQL 실행**: `String sql = "insert into ..."`와 같이 개발자가 직접 SQL문을 작성하여 DB에 전달합니다.
* **리소스 관리**: DB 연결을 위해 `Connection`, `PreparedStatement`, `ResultSet` 객체를 사용하며, 사용 후에는 반드시 `close()`를 호출하여 리소스를 반환해야 메모리 누수를 방지할 수 있습니다.
* **DataSourceUtils 사용**: 스프링 프레임워크의 `DataSourceUtils`를 사용하여 커넥션을 획득하고 릴리즈합니다. 이는 나중에 배울 스프링의 **트랜잭션(Transaction) 관리**를 위해 필수적입니다.
* **복잡한 예외 처리**: JDBC는 `SQLException`이라는 체크 예외를 던지기 때문에 모든 작업에 `try-catch-finally` 블록이 필요하여 코드가 길어지는 단점이 있습니다.

---

## 2. SpringConfig를 통한 구현체 변경과 OCP 원칙

작성하신 `SpringConfig` 파일은 객체 지향 설계의 핵심 원칙 중 하나인 **OCP(Open-Closed Principle)**를 아주 잘 보여주는 예시입니다.

### OCP(개방-폐쇄 원칙)란?
> **"소프트웨어 요소는 확장에는 열려 있으나, 변경에는 닫혀 있어야 한다."**

### 코드 설명
```java
@Bean
public MemberRepository memberRepository() {
    // return new MemoryMemberRepository(); // 기존 메모리 방식
    return new JdbcMemberRepository(dataSource); // 새로운 JDBC 방식
}
```
- 확장(Open): 메모리에 저장하던 방식에서 실제 DB(H2)에 저장하는 방식으로 기능을 확장했습니다. 새로운 JdbcMemberRepository 클래스를 만드는 과정에서 기존의 MemoryMemberRepository 코드는 전혀 수정하지 않았습니다.

- 변경(Closed): 새로운 기능을 적용할 때, 애플리케이션의 전체 코드(특히 MemberService나 Controller)를 수정할 필요가 없습니다. 오직 조립 창고 역할을 하는 SpringConfig의 코드 단 한 줄만 수정하면 됩니다.

### 다형성의 활용
- MemberService는 특정 구현체(Memory 혹은 Jdbc)를 의존하지 않고 인터페이스인 MemberRepository에만 의존합니다. 스프링이 **의존관계 주입(DI, Dependency Injection)**을 통해 실행 시점에 필요한 구현체를 갈아 끼워 주기 때문에 가능한 마법입니다.

- 결과적으로 사용자의 요구사항이 바뀌어 저장소 기술을 변경하더라도, 실제 비즈니스 로직이 담긴 서비스 코드는 안전하게 보호됩니다.