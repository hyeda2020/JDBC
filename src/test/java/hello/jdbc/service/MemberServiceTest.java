package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import hello.jdbc.connection.ConnectionConst;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepositoryV3 memberRepository; // 트랜잭션 기능 O

    @AfterEach
    void after() throws SQLException {
        memberRepository.deleteById("memberA");
        memberRepository.deleteById("memberB");
        memberRepository.deleteById("ex");
    }

    /**
     * 테스트 클래스 안에서
     * 다음과 같은 내부 설정 클래스를 만들고
     * @TestConfiguration 애노테이션을 붙이면
     * 스프링 부트가 자동으로 여기서 등록된 빈들을 추가로 등록해줌으로써
     * 테스트에서 해당 빈들을 사용 가능
     */
    @TestConfiguration
    static class TestConfig{
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberService memberService() {
            return new MemberService(memberRepositoryV3());
        }
    }

    @Test
    @DisplayName("AOP Check")
    void AopCheck() {
        log.info("memberService class={}", memberService.getClass()); // MemberService에 프록시가 적용된 것 확인 가능
        log.info("memberRepository class={}", memberRepository.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(memberService.getClass())).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository.getClass())).isTrue();
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        Member memberA = new Member("memberA", 10000);
        Member memberB = new Member("memberB", 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTranferEX() throws SQLException {
        Member memberA = new Member("memberA", 10000);
        Member memberEx = new Member("ex", 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        assertThatThrownBy(() ->
                memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}
