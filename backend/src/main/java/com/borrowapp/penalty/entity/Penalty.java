package com.borrowapp.penalty.entity;

import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalties")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_request_id")
    private BorrowRequest borrowRequest; // nullable với MANUAL_ADJUSTMENT

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20)
    private PenaltyType type = PenaltyType.AUTO;

    @CreationTimestamp
    private LocalDateTime createdAt;
}