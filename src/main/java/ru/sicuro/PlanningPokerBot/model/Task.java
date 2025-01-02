package ru.sicuro.PlanningPokerBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private String title;

    private String description;
    private String status = "pending"; // 'pending', 'completed'
    private Integer finalEstimate;
    private String link;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    // Getters and Setters
}