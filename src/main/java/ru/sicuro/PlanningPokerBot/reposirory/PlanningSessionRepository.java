package ru.sicuro.PlanningPokerBot.reposirory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sicuro.PlanningPokerBot.model.PlanningSession;
import ru.sicuro.PlanningPokerBot.model.Team;

import java.util.List;

@Repository
public interface PlanningSessionRepository extends JpaRepository<PlanningSession, Long> {
    List<PlanningSession> findByTeamAndStatus(Team team, String status);
}