package org.shax3.square.domain.debate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "debate")
@SQLRestriction("is_valid = true")
public class Debate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "is_valid", nullable = false)
    private boolean valid;

    @Column(nullable = false)
    private String leftOption;

    @Column(nullable = false)
    private String rightOption;

    @Builder
    public Debate(String topic, String leftOption, String rightOption, Category category) {
        this.topic = topic;
        this.leftOption = leftOption;
        this.rightOption = rightOption;
        this.category = category;
        this.valid = true;
    }
}
