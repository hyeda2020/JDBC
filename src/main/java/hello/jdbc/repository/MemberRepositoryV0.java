package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

import static hello.jdbc.connection.DBConnectionUtil.*;

/**
 * JDBC - DriverManager 사용
 */

@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {

        /**
         * 만약 ? <- 이와 같은 파라미터 바인딩을 쓰지 않고 아래처럼
         * values(" + memberId + ", " + money + "); 와 같이 String 변수를 직접 넣을 경우
         * ... values(select * from ...); 이런식으로
         * SQL Injection을 통해 memberId 변수에 SQL문이 삽입되어 의도치 않은 DB 조회가 발생할 수 있으므로,
         * PreparedStatement를 통해 파라미터 바인딩을 쓰면 SQL Injection이 발생하여도
         * ... values("select * from ..."); 이렇게 값으로 들어가게 되므로 안전
         */
        String sql = "insert into Member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); //  Statement 를 통해 준비된 SQL을 커넥션을 통해 실제 데이터베이스에 전달
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null); // Connection, Statement 리소스 정리
        }

    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        /* 각각의 리소스 정리 로직을 try-catch 문으로 감싸야 함 */
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }
}
