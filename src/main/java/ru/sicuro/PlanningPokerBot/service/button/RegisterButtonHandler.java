package ru.sicuro.PlanningPokerBot.service.button;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.RegistrationState;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;

@Slf4j
@Service
@AllArgsConstructor
public class RegisterButtonHandler implements ButtonHandler {
    private final UserRepository userRepository;

    @Override
    public String getCallbackData() {
        return "REGISTER_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);

        // Проверяем, не зарегистрирован ли пользователь
        var queueUser = userRepository.findByChatId(chatId);
        User user = queueUser.get();
        var registrationState = user.getRegistrationState();
        if (registrationState != null && registrationState.equals(RegistrationState.REGISTERED)) {
            message.setText("Вы уже зарегистрированы😊!");
            bot.sendMessage(message);
            return;
        }

        // Устанавливаем состояние пользователя
        user.setRegistrationState(RegistrationState.WAITING_FOR_NAME);
        userRepository.save(user);

        // Устанавливаем новый текст
        message.setText("Пожалуйста, введите ваше Фамилию и Имя:");
        bot.sendMessage(message);
        log.info("Пользователь({}({})) начал регистрацию", chatId, user.getUsername());
    }
}