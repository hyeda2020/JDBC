package hello.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 자바 예외 처리 원칙
 * 1) 기본은 언체크 예외(런타임 익셉션) 사용
 * 2) 정말 중요하고 놓치면 안되는, 꼭 처리해야 되는 문제일 경우에만 체크 예외 사용
 *
 * 가령, 레포지토리에서 DB서버 다운 등으로 인해 SQLException 체크 예외를 던지면,
 * 그 위 계층인 서비스, 컨트롤러에서 이런 문제를 해결할 수 없음에도 강제적으로 해당 예외에 의존하게 됨.
 * 또한, 이로 인해 DB 기술 변경으로 SQLException이 다른 종류의 익셉션으로 바뀔 경우
 * 체크 예외에 의존하는 서비스, 컨트롤러에서 해당 예외를 throw 하는 소스를 모두 고쳐야 함
 */

@Slf4j
public class UncheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();
        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();
        public void logic() {
            // 둘 다 언체크 예외이기 때문에 서비스에서 자신이 해결할 수 없는 예외를 신경쓰지 않아도 됨
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() {
            /**
             *  체크 예외를 잡아서 런타임 예외로 던짐
             */
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        }
        private void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
