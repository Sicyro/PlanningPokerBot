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
public class MyTeamRenameButtonHandler implements ButtonHandler {
    private static final Map<Long, Team> renameTeam = new ConcurrentHashMap<>();
    private final TeamRepository teamRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAM_RENAME_BUTTON";
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
        message.setText("Введите новое название команды:");

        // Получим данные для работы с командой
        var optionalTeam = teamRepository.findById(Long.valueOf(teamId));
        Team team;
        if (optionalTeam.isEmpty()) {
            log.warn("Команда не найдена по id {}", teamId);
            return;
        } else {
            team = optionalTeam.get();
        }

        renameTeam.put(chatId, team);

        bot.sendMessage(message);
        log.info("Пользователь({}) начал переименовывание команды", chatId);
        bot.setUserState(chatId, UserState.TEAM_WAITING_FOR_NEW_NAME);

    }

    public void handleRenameTeamStep(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        var state = bot.getUserState(chatId);

        // Если пользователь не начинал создание команды выходим
        if (state == null) {
            return;
        }

        if (!UserState.TEAM_WAITING_FOR_NEW_NAME.equals(state)) {
            return;
        }

        // Получим команду
        Team team = renameTeam.remove(chatId);
        team.setName(messageText);
        teamRepository.save(team);

        log.info("Команда переименована: {}", team);
        bot.sendMessage(chatId, "Команда переименована! \n" +
                "Посмотреть список команд можно в меню команд \"Мои команды\"");
        bot.deleteUserState(chatId);
    }
}
