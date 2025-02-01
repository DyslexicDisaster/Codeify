package codeify.business;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @EqualsAndHashCode.Include
    @NonNull
    private int userId;
    private String username;
    @ToString.Exclude
    private String password;
    private String salt;
    private String email;
    private LocalDate registrationDate;
    private role role;

    public User(String username, String password, String salt, String email, LocalDate registrationDate) {
        userId = 0;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.email = email;
        this.registrationDate = registrationDate;
        this.role = role.user;
    }
}
