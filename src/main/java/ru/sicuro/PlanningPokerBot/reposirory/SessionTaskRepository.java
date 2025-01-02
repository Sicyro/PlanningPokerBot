package ru.sicuro.PlanningPokerBot.reposirory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sicuro.PlanningPokerBot.model.PlanningSession;
import ru.sicuro.PlanningPokerBot.model.SessionTask;
import ru.sicuro.PlanningPokerBot.model.SessionTaskId;

import java.util.List;

@Repository
public interface SessionTaskRepository extends JpaRepository<SessionTask, SessionTaskId> {
    List<SessionTask> findBySession(PlanningSession session);
}