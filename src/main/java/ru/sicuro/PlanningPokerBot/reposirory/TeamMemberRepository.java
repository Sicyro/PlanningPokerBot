package ru.sicuro.PlanningPokerBot.reposirory;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.TeamMember;
import ru.sicuro.PlanningPokerBot.model.TeamMemberId;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    List<TeamMember> findByTeam(Team team);

    @Modifying
    @Transactional
    @Query("DELETE FROM TeamMember tm WHERE tm.team = :team")
    void deleteByTeam(@Param("team") Team team);
}