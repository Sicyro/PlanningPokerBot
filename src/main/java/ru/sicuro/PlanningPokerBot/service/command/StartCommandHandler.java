package ru.sicuro.PlanningPokerBot.service.command;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.Role;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class StartCommandHandler implements CommandHandler {
    private final UserRepository userRepository;

    @Override
    public String getCommandName() {
        return "/start";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();

        // Регистрируем пользователя
        registerUser(chatId, userName);

        // Получим данные пользователя
        var queueUser = userRepository.findByChatId(chatId);
        User user = queueUser.get();

        // Формируем ответное сообщение
        String welcomeMessage = String.format("Привет, %s✌️! Добро пожаловать в бот планирования Poker!",
                user.getFullNameOrUsername());

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(welcomeMessage);

        // Отправляем сообщение пользователю
        bot.sendMessage(message);
    }

    private void registerUser( long chatId, String userName) {
        // Если пользователь уже есть не регистрируем нового
        if (userRepository.findByChatId(chatId).isPresent()) {
            return;
        }

        // Создаём нового пользователя
        User newUser = User.createUser(chatId, userName);

        // Сохраняем пользователя в базу данных
        userRepository.save(newUser);
        log.info("Новый пользователь зарегистрирован: {}", newUser);
    }
}
