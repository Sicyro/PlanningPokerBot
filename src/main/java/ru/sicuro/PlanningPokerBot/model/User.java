package ru.sicuro.PlanningPokerBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Entity(name = "pp_user")
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long chatId;

    private String username;
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // team_lead, member

    @Enumerated(EnumType.STRING)
    private RegistrationState registrationState;

    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFullNameOrUsername() {
        if (fullName == null) {
            return username;
        } else {
            return fullName;
        }
    }

    public static User createUser(long chatId, String userName) {
        // Создаём нового пользователя
        User newUser = new User();
        newUser.setChatId(chatId);
        newUser.setUsername("@" + userName);
        newUser.setRole(Role.GUEST); // По умолчанию роль "GUEST"
        newUser.setCreatedAt(LocalDateTime.now());

        return newUser;
    }
}
