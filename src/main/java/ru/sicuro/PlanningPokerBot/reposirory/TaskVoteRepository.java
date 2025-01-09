package ru.sicuro.PlanningPokerBot.reposirory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sicuro.PlanningPokerBot.model.Task;
import ru.sicuro.PlanningPokerBot.model.TaskVote;

import java.util.List;

@Repository
public interface TaskVoteRepository extends JpaRepository<TaskVote, Long> {
    List<TaskVote> findByTask(Task task);

    @Query("""
            SELECT AVG(tv.vote) 
            FROM TaskVote tv 
            WHERE tv.task = :task AND tv.vote IS NOT NULL""")
    Double findAverageVoteByTask(Task task);
}