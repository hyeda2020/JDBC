package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.jdbc.support.JdbcUtils.*;

/**
 * JDBC - DataSource 사용
 */

@Slf4j
public class MemberRepositoryV1 {

    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource) {
        // 어느 DataSource 구현체라도 유연하게 사용할 수 있게 DataSource 인터페이스에 의존
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        /**
         * 커넥션 풀에서 커넥션을 생성하는 작업은
         * 애플리케이션 실행 속도에 영향을 주지 않기 위해 별도의 쓰레드에서 작동.
         */
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        /* JdbcUtils 활용 */
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(con);
    }

    public Member save(Member member) throws SQLException {

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
}
