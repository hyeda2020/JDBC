package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import javax.sql.DataSource;

/**
 * <JdbcTemplate 적용>
 * JDBC의 반복적인 작업(트랜잭션을 위한 커넥션 동기화, 스프링 예외 변환기 등)을
 * 스프링에서 효과적으로 처리해주는 템플릿 콜백 패턴
 * -> 코드 중복 제거!
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{

    private JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate((dataSource));
    }

    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";
        int updatedCnt = template.update(sql, member.getMemberId(), member.getMoney());
        log.info("updatedCnt={}", updatedCnt);
        return member;
    }

    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }

    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }

    public void deleteById(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }

    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }
}
