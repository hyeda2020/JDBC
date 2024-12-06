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

- JDBC DriverManager : JDBC가 제공하는 `DriverManager` 는 라이브러리에 등록된 DB 드라이버들을 관리하고 커넥션을 획득하는 기능 제공  
  1) 애플리케이션 로직에서 커넥션이 필요하면 `DriverManager.getConnection()` 을 호출
  2) DriverManager는 라이브러리에 등록된 드라이버들에게 URL, DB이름, 비밀번호 등의 정보를 넘겨서 커넥션을 획득할 수 있는지 확인
  3) 이렇게 찾은 커넥션 구현체가 클라이언트에 반환
![image](https://github.com/user-attachments/assets/f078dbc7-2c1d-429b-9081-d71ffb4c02a4)

