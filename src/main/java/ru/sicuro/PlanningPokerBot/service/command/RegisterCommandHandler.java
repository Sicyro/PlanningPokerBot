package ru.sicuro.PlanningPokerBot.service.command;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.RegistrationState;
import ru.sicuro.PlanningPokerBot.model.Role;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.UserState;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class RegisterCommandHandler implements CommandHandler {
    private final UserRepository userRepository;

    @Override
    public String getCommandName() {
        return "/register";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();

        // Проверяем, не зарегистрирован ли пользователь
        var queueUser = userRepository.findByChatId(chatId);
        User user;
        if (queueUser.isEmpty()) {
            String userName = update.getMessage().getChat().getUserName();

            // Создаём нового пользователя
            user = registerUser(chatId, userName);
        } else {
            user = queueUser.get();
        }

        var registrationState = user.getRegistrationState();
        if (registrationState != null && registrationState.equals(RegistrationState.REGISTERED)) {
            bot.sendMessage(chatId, "Вы уже зарегистрированы😊!");
            return;
        }

        // Устанавливаем состояние пользователя
        user.setRegistrationState(RegistrationState.WAITING_FOR_NAME);
        userRepository.save(user);

        // Установим статус регистрации
        bot.setUserState(chatId, UserState.REGISTRATION_WAITING_FOR_NAME);

        bot.sendMessage(chatId, "Пожалуйста, введите вашу Фамилию и Имя:");
        log.info("Пользователь({}({})) начал регистрацию", chatId, user.getUsername());
    }

    public void handleRegistrationStep(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        var state = bot.getUserState(chatId);

        // Если пользователь не начинал регистрации выходим
        if (state == null) {
            return;
        }

        if (!UserState.REGISTRATION_WAITING_FOR_NAME.equals(state)) {
            return;
        }

        // Завершаем регистрацию
        var queueUser = userRepository.findByChatId(chatId);
        User user = queueUser.get();
        completeRegistration(user, messageText, bot);
        bot.deleteUserState(chatId);
    }

    private void completeRegistration(User user, String fullName, PlanningPokerBot bot) {
        // Обновляем данные пользователя
        user.setRegistrationState(RegistrationState.REGISTERED);
        user.setFullName(fullName);
        user.setRole(Role.MEMBER); // Для зарегистрированных пользователей задаём роль MEMBER

        // Сохраняем пользователя в базу данных
        userRepository.save(user);
        log.info("Данные о пользователе обновлены: {}", user);

        bot.sendMessage(user.getChatId(), "Регистрация успешно завершена😊! Добро пожаловать, " + fullName + "!");
    }

    private User registerUser( long chatId, String userName) {
        // Создаём нового пользователя
        User newUser = User.createUser(chatId, userName);

        // Сохраняем пользователя в базу данных
        userRepository.save(newUser);
        log.info("Новый пользователь зарегистрирован (перед регистрацие): {}", newUser);

        return newUser;
    }
}
