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
        User user = queueUser.get();
        var registrationState = user.getRegistrationState();
        if (registrationState != null && registrationState.equals(RegistrationState.REGISTERED)) {
            bot.sendMessage(chatId, "Вы уже зарегистрированы😊!");
            return;
        }

        // Устанавливаем состояние пользователя
        user.setRegistrationState(RegistrationState.WAITING_FOR_NAME);
        userRepository.save(user);

        bot.sendMessage(chatId, "Пожалуйста, введите ваше Фамилию и Имя:");
        log.info("Пользователь({}({})) начал регистрацию", chatId, user.getUsername());
    }

    public void handleRegistrationStep(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        var queueUser = userRepository.findByChatId(chatId);
        User user = queueUser.get();
        var state = user.getRegistrationState();

        // Если пользователь не начинал регистрации выходим
        if (state == null) {
            return;
        }

        if (RegistrationState.WAITING_FOR_NAME.equals(state)) {
            completeRegistration(user, messageText, bot);
        }
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
}
