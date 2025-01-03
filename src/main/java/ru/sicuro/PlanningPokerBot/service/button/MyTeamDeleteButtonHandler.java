package ru.sicuro.PlanningPokerBot.service.button;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.UserState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@AllArgsConstructor
public class MyTeamDeleteButtonHandler implements ButtonHandler {
    private static final Map<Long, Team> deleteTeam = new ConcurrentHashMap<>();
    private final TeamRepository teamRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAM_DELETE_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String teamId = callbackQuery.split(" ")[1];

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);

        // Получим данные для работы с командой
        var optionalTeam = teamRepository.findById(Long.valueOf(teamId));
        Team team;
        if (optionalTeam.isEmpty()) {
            log.warn("Команда не найдена по id {}", teamId);
            return;
        } else {
            team = optionalTeam.get();
        }

        message.setText(
                String.format("Для подтверждения удаления введите название команды (%s)",
                        team.getName()));
        deleteTeam.put(chatId, team);
        bot.sendMessage(message);
        log.info("Пользователь({}) начал удаление команды", chatId);
        bot.setUserState(chatId, UserState.TEAM_WAITING_DELETE);

    }

    public void handleDeleteTeamStep(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        var state = bot.getUserState(chatId);

        // Если пользователь не начинал создание команды выходим
        if (state == null) {
            return;
        }

        if (!UserState.TEAM_WAITING_DELETE.equals(state)) {
            return;
        }

        // Получим команду
        Team team = deleteTeam.remove(chatId);

        if (!team.getName().equals(messageText)) {
            bot.sendMessage(chatId, "Введено не корректное имя команды!");
            bot.deleteUserState(chatId);
            return;
        }

        teamRepository.delete(team);

        log.info("Команда удалена: {}", team);
        bot.sendMessage(chatId, "Команда удалена! \n" +
                "Посмотреть список команд можно в меню команд \"Мои команды\"");
        bot.deleteUserState(chatId);
    }
}
