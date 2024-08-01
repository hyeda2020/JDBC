package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

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