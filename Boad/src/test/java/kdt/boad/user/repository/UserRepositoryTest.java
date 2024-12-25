package kdt.boad.user.repository;

import kdt.boad.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // 트랜잭션 마치면 롤백 수행, 레포지토리 테스트 수행 용도
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("저장된 사용자 ID 존재 여부 확인 - 존재하는 사용자 : True")
    void existsById_return_true() {
        // Given
        User saveUser = User.builder()
                .id("testUser1")
                .password("TestUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(saveUser);

        // When
        boolean exist = userRepository.existsById("testUser1");

        // Then
        assertTrue(exist);
    }

    @Test
    @DisplayName("저장된 사용자 ID 존재 여부 확인 - 존재하는 사용자 : False")
    void existsById_return_false() {
        // Given
        User saveUser = User.builder()
                .id("testUser1")
                .password("TestUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(saveUser);

        // When
        boolean exist = userRepository.existsById("testUser2");

        // Then
        assertFalse(exist);
    }

    @Test
    @DisplayName("저장된 사용자 닉네임 존재 여부 확인 - 존재하는 사용자")
    void existsByNickname_exist_user() {
        // Given
        User saveUser = User.builder()
                .id("testUser1")
                .password("TestUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(saveUser);

        // When
        boolean exist = userRepository.existsByNickname("testUser1");

        // Then
        assertTrue(exist);
    }

    @Test
    @DisplayName("저장된 사용자 닉네임 존재 여부 확인 - 존재하지 않는 사용자")
    void existsByNickname_non_exist_user() {
        // Given
        User saveUser = User.builder()
                .id("testUser1")
                .password("TestUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(saveUser);

        // When
        boolean exist = userRepository.existsByNickname("testUser2");

        // Then
        assertFalse(exist);
    }

    @Test
    @DisplayName("id를 통한 사용자 조회 - 존재하지 않는 사용자")
    void findById_non_exist_user() {
        // Given
        User saveUser = User.builder()
                .id("testUser1")
                .password("TestUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(saveUser);

        // When
        User findUser = userRepository.findById("NoneUser");

        // Then
        assertThat(findUser).isEqualTo(null);
    }

    @Test
    @DisplayName("id를 통한 사용자 조회 - 존재하는 사용자")
    void findById_exist_user() {
        // Given
        User saveUser = User.builder()
                .id("testUser1")
                .password("TestUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(saveUser);

        // When
        User findUser = userRepository.findById("testUser1");

        // Then
        assertThat(findUser).isEqualTo(saveUser);
    }
}