package codeify.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    //Reference for migrations: https://www.youtube.com/watch?v=WSnnJeqGtOQ

    @EqualsAndHashCode.Include
    @NonNull
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("user_id")
    private int userId;

    @Column(name = "username")
    private String username;

    @ToString.Exclude
    @Column(name = "password")
    private String password;

    @Column(name = "salt")
    private String salt;

    @Column(name = "email")
    private String email;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private role role;

    public User(String username, String password, String salt, String email, LocalDate registrationDate) {
        this.userId = 0;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.email = email;
        this.registrationDate = registrationDate;
        this.role = role.user;
    }
}