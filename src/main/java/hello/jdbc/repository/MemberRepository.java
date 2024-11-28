package hello.jdbc.repository;

import hello.jdbc.domain.Member;

public interface MemberRepository {

    public Member save(Member member);
    public Member findById(String memberId);
    public void update(String memberId, int money);
    public void deleteById(String memberId);
}
