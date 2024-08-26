package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class MemberService {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberService(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        /**
         * 트랜잭션 템플릿 적용
         * 언체크 예외가 발생하면 롤백. 그 외의 경우 (체크 예외가 발생하여도) 커밋 -> 스프링 기본 원칙
         * 덕분에 커밋/롤백 소스를 서비스에서 제외시킬 수 있다는 장점이 있지만,
         * 여전히 트랜잭션을 처리하는 기술 로직이 서비스에 포함되어 있다는 단점 존재.
         */
        txTemplate.executeWithoutResult((status) -> {
            try {
                //비즈니스 로직
                bizLogic(fromId, toId, money);
            } catch (SQLException e) { // 체크 예외가 발생하면 언체크 예외를 던져서 롤백되도록
                throw new IllegalStateException(e);
            }
        });
    }
    private void bizLogic(String fromId, String toId, int money)
            throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }
    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); //커넥션 풀 고려
                con.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}