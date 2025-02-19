package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.StepHandler;
import ru.sicuro.PlanningPokerBot.service.UserState;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@AllArgsConstructor
public class MyTeamRenameButtonHandler implements ButtonHandler, StepHandler {
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
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));

        renameTeam.put(chatId, team);

        bot.sendMessage(message);
        log.info("Пользователь({}) начал переименовывание команды", chatId);
        bot.setUserState(chatId, UserState.TEAM_WAITING_FOR_NEW_NAME);

    }

    @Override
    public void handleStep(Update update, PlanningPokerBot bot) {
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
