package codeify.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "users")
public class User implements UserDetails {

    @EqualsAndHashCode.Include
    @NonNull
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @ToString.Exclude
    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true, length = 100, nullable = false)
    private String email;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('admin', 'user') DEFAULT 'user'")
    private role role;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    public User(String username, String password, String email, LocalDate registrationDate, role role, String provider) {
        this.username = (username != null) ? username : "unknown";
        this.password = (password != null) ? password : "";
        this.email = (email != null) ? email : "unknown@example.com";
        this.registrationDate = (registrationDate != null) ? registrationDate : LocalDate.now();
        this.role = (role != null) ? role : role.user;
        this.provider = (provider != null) ? provider : "unknown";
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}