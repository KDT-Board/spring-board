package kdt.boad.user.repository;

import kdt.boad.user.domain.Grade;
import kdt.boad.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsById(String id);
    boolean existsByNickname(String nickname);
    User findById(String id);
    User findByNickname(String nickname);
    long count();
    Long countByGrade(Grade grade);
}
