package org.shax3.square.domain.debate.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyDebate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debate_id", nullable = false)
    private Debate debate;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Builder
    public DailyDebate(Debate debate) {
        this.debate = debate;
        this.date = LocalDate.now();
    }
}
