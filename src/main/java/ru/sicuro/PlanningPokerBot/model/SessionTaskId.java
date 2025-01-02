package ru.sicuro.PlanningPokerBot.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class SessionTaskId implements Serializable {
    private Long sessionId;
    private Long taskId;
}