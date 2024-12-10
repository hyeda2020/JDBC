# JDBC  
리포지토리 설명 : 인프런 김영한님의 강의 '스프링 DB 1편 - 데이터 접근 핵심 원리' 스터디 정리  
https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-1/dashboard  
  
# 1. JDBC 표준 인터페이스
JDBC(Java Database Connectivity)는 자바에서 데이터베이스에 접속할 수 있도록 하는 인터페이스이며,  
이 JDBC 인터페이스를 각각의 DB 회사(MySQL, Oracle 등)에서 자신의 DB에 맞도록 구현해서 라이브러리로 제공하는데,  
이것을 JDBC 드라이버라 함.  
![image](https://github.com/user-attachments/assets/aed8a687-1a6c-42b7-9e83-8adb5b9768f0)


이를 통해 애플리케이션 로직은 JDBC 표준 인터페이스에만 의존하면 되며,  
다른 종류의 데이터베이스로 변경하고 싶으면 JDBC 구현 라이브러리만 변경하면  
애플리케이션 서버의 사용 코드를 그대로 유지 가능.  

※ 최근엔 JDBC를 직접 사용하기 보다는 SQL Mapper나 ORM 기술을 활용하는 추세  

- JDBC DriverManager : JDBC가 제공하는 `DriverManager`는 라이브러리에 등록된 DB 드라이버들을 관리하고 커넥션을 획득하는 기능 제공  
  1) 애플리케이션 로직에서 커넥션이 필요하면 `DriverManager.getConnection()` 을 호출
  2) DriverManager는 라이브러리에 등록된 드라이버들에게 URL, DB이름, 비밀번호 등의 정보를 넘겨서 커넥션을 획득할 수 있는지 확인
  3) 이렇게 찾은 커넥션 구현체가 클라이언트에 반환  
![image](https://github.com/user-attachments/assets/f078dbc7-2c1d-429b-9081-d71ffb4c02a4)

# 2. 커넥션풀과 데이터소스 이해  

- 커넥션 풀 이해  
  애플리케이션 로직에서 DB 커넥션을 획득할 때에는 다음과 같은 복잡한 과정을 거침
  1) DB드라이버를 통해 DB와 TCP/IP 커넥션 연결(이 과정에서 3-way handshake 와 같은 네트워크 동작 발생)
  2) ID, PW와 기타 부가정보를 DB에 전달하여 내부 인증 완료  
  3) DB는 내부에 세션을 생성하고 커넥션 생성 완료
  4) DB드라이버는 커넥션 객체를 생성해서 클라이언트에 반환

  이러한 과정은 고객이 애플리케이션을 사용할 때, SQL을 실행하는 시간 뿐만 아니라  
  커넥션을 새로 만드는 시간이 추가되기 때문에 결과적으로 응답 속도에 영향을 줌.  
  이런 문제를 해결하기 위한 것이 바로 커넥션을 미리 생성해두고 사용하는 '커넥션 풀' 이라는 방법  
![image](https://github.com/user-attachments/assets/ffcd99e8-9eac-49c9-9a4b-92a6c59bdcd3)

※ 적절한 커넥션 풀 숫자는 서비스의 특징과 애플리케이션/DB 서버 스펙에 따라 다르기 때문에 성능 테스트를 통해서 정해야 함.  

- DataSource 이해  
![image](https://github.com/user-attachments/assets/518b00b4-c104-4dcc-a15b-3a27bc89b2eb)  
  커넥션을 얻는 방법은 `DriverManager`나 `HikariCP` 등을 사용하는 등 다양한 방법이 있음.  
  만약 애플리케이션 로직에서 `DriverManager`를 통해 커넥션을 얻다가 `HikariCP`와 같은 커넥션 풀을 사용하는 방법으로 바뀔 경우  
   `DriverManager`에 의존하는 소스코드를 모두 고쳐야 하는 단점이 있음.

  자바에서는 이러한 문제를 해결하기 위해 `DataSource`라는 커넥션을 획득하는 방법을 추상화 하는 인터페이스 제공.  
  따라서 개발자는 `DriverManager`나 `HikariCP`에 직접 의존하지 않고 DataSource 인터페이스에만 의존하도록  
  애플리케이션 로직을 작성하면 됨.  
  스프링에서는 `DriverManager`도 `DataSource`를 통해서 사용할 수 있도록 `DriverManagerDataSource`라는  
  `DataSource`를 구현한 클래스를 제공.  

# 3. 스프링과 트랜잭션
주로 사용하는 애플리케이션 구조는 프레젠테이션-서비스-데이터 계층 구조이며,  
특히 여기서 비즈니스 로직이 들어 있는 서비스 계층은 가급적 특정 기술에 의존하지 않고 순수한 자바 코드로 작성되어야  
추후 기술을 변경하더라도 비즈니스 로직 변경 없이 유지가 가능.  

DB접근 기술에서 가장 중요한 트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작하는 것이 좋지만,  
한편으로는 이로 인해 서비스 계층이 트랜잭션 사용을 위해 아래 그림처럼 `javax.sql.DataSource`, 혹은  
`java.sql.Connection`와 같은 기술에 의존하게 되는 문제 발생  
![스크린샷 2024-12-10 211021](https://github.com/user-attachments/assets/56794ddd-ee7b-4674-9668-5c8c53de4dff)

- 트랜잭션 매니저 : 스프링이 제공하는 트랜잭션 매니저는 크게 2가지 역할을 함.  
1) 트랜잭션 추상화 : 서비스가 특정 트랜잭션 기술에 의존하는 것이 아닌, 추상화된 인터페이스에 의존  
![스크린샷 2024-12-10 212315](https://github.com/user-attachments/assets/3a0596b9-07e1-46ff-9756-e854f7c173c2)

2) 리소스 동기화 : 트랜잭션을 유지하려면 트랜잭션의 시작부터 끝까지 같은 데이터베이스 커넥션을 유지해야 하는데,  
   이를 위해선 로직 수행시 매번 커넥션 유지를 위해 파라미터로 커넥션을 넘겨주는 방법을 사용해야 하지만,  
   트랜잭션 동기화 매니저를 사용하면 이러한 과정 없이 서비스 시작부터 끝까지 커넥션 동기화 가능.
![스크린샷 2024-12-10 212602](https://github.com/user-attachments/assets/9c97a9fb-d09d-4421-bf05-1ed7716ca437)

=> 단, 트랜잭션 매니저를 활용하더라도 여전히 비즈니스 로직에 트랜잭션 기술 소스코드가 포함되어 있다는 문제가 있음.  

- 스프링이 제공하는 트랜잭션 AOP  
트랜잭션 처리가 필요한 곳에 `@Transactional`애노테이션만 붙여주면, 서비스 계층에서 직접 트랜잭션을 시작하는 로직을 걷어낼 수 있음.   
```
@Transactional
public void accountTransfer(String fromId, String toId, int money) throws SQLException {
  bizLogic(fromId, toId, money);
}
```
