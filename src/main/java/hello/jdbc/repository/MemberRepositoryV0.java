package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.DBConnectionUtil.*;

/**
 * JDBC - DriverManager 사용
 */

@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {

        /**
         * 만약 '?' <- 이와 같은 파라미터 바인딩을 쓰지 않고 아래처럼
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

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from Member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        con = getConnection();
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); // 데이터를 변경할때는 executeUpdate()를, 조회할때는 executeQuery() 사용

            /**
             * <ResultSet>
             * 쿼리 결과를 순서대로 담고 있으며,
             * 내부의 커서를 통해 데이터에 하나씩 접근 가능
             * 단, 최초의 커서는 데이터를 가리키고 있지 않기 때문에
             * rs.next() 를 반드시 최초 한번은 호출해야 데이터 조회 가능
             */
            if (rs.next()) { // ResultSet 내부의 cursor가 다음 row로 이동
                // 해당 memberId를 가진 Member 하나만 찾는 것이기 때문에 찾으면 바로 리턴
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public List<Member> findAll() throws SQLException {
        String sql = "select * from Member";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        con = getConnection();
        try {
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            List<Member> memberList = new ArrayList<>();
            while (rs.next()) {
                Member member = new Member(
                        rs.getString("member_Id"),
                        rs.getInt("money")
                );
                memberList.add(member);
            }
            return memberList;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update Member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        con = getConnection();
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.info("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void deleteById(String memberId) throws SQLException {
        String sql = "delete from Member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        con = getConnection();
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.info("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
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
