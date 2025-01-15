package kdt.boad.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long number;

    private String id;

    private String password;

    private String nickname;

    private Grade grade;

    @Builder
    private User(String id, String password, String nickname) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.grade = Grade.BRONZE;
    }

    public static User of(String id, String password, String nickname) {
        return User.builder()
                .id(id)
                .password(password)
                .nickname(nickname)
                .build();
    }
}
