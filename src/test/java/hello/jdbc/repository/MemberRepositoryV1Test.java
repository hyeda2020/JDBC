package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach() {
        // 커넥션 풀링: HikariProxyConnection -> JdbcConnection
        HikariDataSource dataSource = new HikariDataSource(); // 실무에서는 주로 커넥션 풀을 사용하게 해주는 HikariCP 사용
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
//        dataSource.setMaximumPoolSize(10); // 풀사이즈 미지정시 디폴트값은 최대 10개
        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException {
        // save
        Member member = new Member("memberV1", 10000);
        repository.save(member);

        // findById
        Member findedMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findedMember);
        assertThat(findedMember).isEqualTo(member); // Member 클래스에 @Data 애노테이션이 있기 때문에 비교 가능

        // findAll
        List<Member> memberList = repository.findAll();
        for (Member m : memberList) {
            log.info("member_id={}", m.getMemberId());
        }

        // update
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // delete
        repository.deleteById(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class); // NoSuchElementException이 터지면 테스트 성공
    }
}