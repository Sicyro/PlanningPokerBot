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
public class Task implements Viewer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskState status = TaskState.PENDING;
    private String link;
    private Integer finalEstimate;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public String getDescription() {
        if (description == null) {
            return "-";
        } else {
            return description;
        }
    }

    public String getDescriptionHtml() {
        return Viewer.escapeHtml(getDescription());
    }

    @Override
    public String getViewHtml() {

        if (link != null) {
            return "🎯<a href='" + Viewer.escapeHtml(link) + "'>" + Viewer.escapeHtml(title) + "</a>";
        } else {
            return getView();
        }
    }

    @Override
    public String getView() {
        return "🎯" + Viewer.escapeHtml(title);
    }
}