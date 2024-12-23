package kdt.boad.user.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long number;

    private String id;

    private String password;

    private String nickname;

    private Grade grade;

    @Builder
    public User(String id, String password, String nickname) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.grade = Grade.BRONZE;
    }
}
