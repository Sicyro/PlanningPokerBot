package ru.sicuro.PlanningPokerBot.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class TeamMemberId implements Serializable {
    private Long teamId;
    private Long userId;
}