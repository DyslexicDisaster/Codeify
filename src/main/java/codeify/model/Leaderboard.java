package codeify.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "leaderboard")
public class Leaderboard {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private ProgrammingLanguage language;

    @Column(name = "total_score")
    private int totalScore = 0;

    @Column(name = "rank")
    private int rank;

    @CreationTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
