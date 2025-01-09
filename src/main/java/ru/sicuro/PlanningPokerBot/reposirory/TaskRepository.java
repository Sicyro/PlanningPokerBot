package ru.sicuro.PlanningPokerBot.reposirory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sicuro.PlanningPokerBot.model.Task;
import ru.sicuro.PlanningPokerBot.model.TaskState;
import ru.sicuro.PlanningPokerBot.model.Team;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTeamAndStatus(Team team, TaskState status);

    List<Task> findByTeam(Team team);

    void deleteByTeam(Team team);
}