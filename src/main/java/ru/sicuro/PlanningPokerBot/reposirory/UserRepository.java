package ru.sicuro.PlanningPokerBot.reposirory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatId(long chatId);

    @Query("""
            SELECT u FROM pp_user u
            WHERE u.id NOT IN (
                SELECT tm.user.id FROM TeamMember tm WHERE tm.team = :teamId
            )
            AND u != :currentUserId
            AND (u.role = "MEMBER" or u.role = "TEM_LEAD")
        """)
    List<User> findUsersNotInTeamAndNotCurrentUser(@Param("teamId") Team team,
                                                   @Param("currentUserId") User currentUser);
}
