package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.UserState;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

@Slf4j
@Service
@AllArgsConstructor
public class CreateTeamButtonHandler implements ButtonHandler {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public String getCallbackData() {
        return "CREATE_TEAM_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Введите название команды:");

        bot.sendMessage(message);
        log.info("Пользователь({}) начал создание команды", chatId);
        bot.setUserState(chatId, UserState.TEAM_WAITING_FOR_NAME);
    }

    public void handleCreateTeamStep(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        var state = bot.getUserState(chatId);

        // Если пользователь не начинал создание команды выходим
        if (state == null) {
            return;
        }

        if (!UserState.TEAM_WAITING_FOR_NAME.equals(state)) {
            return;
        }

        // Получим пользователя
        var queueUser = userRepository.findByChatId(chatId);
        User user = queueUser.get();

        // Создадим команду
        Team team = new Team();
        team.setName(messageText);
        team.setCreatedBy(user);

        // Сохраним данные
        teamRepository.save(team);
        log.info("Создана новая команда: {}", team);
        bot.sendMessage(chatId, "Новая команда создана! \n" +
                "Посмотреть список команд можно в меню команд \"Мои команды\"");

        // Сбросим статус
        bot.deleteUserState(chatId);
    }
}
