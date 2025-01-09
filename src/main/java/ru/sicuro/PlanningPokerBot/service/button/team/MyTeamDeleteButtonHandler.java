package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.reposirory.TaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamMemberRepository;
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
public class MyTeamDeleteButtonHandler implements ButtonHandler, StepHandler {

    private static final Map<Long, Team> deleteTeam = new ConcurrentHashMap<>();
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;

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
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));

        message.setText(
                String.format("""
                                ❗Вместе с командой удалятся данные о задачах и участниках команды!
                                
                                Для подтверждения удаления введите название команды (%s)
                                """,
                        team.getName()));
        deleteTeam.put(chatId, team);
        bot.sendMessage(message);
        log.info("Пользователь({}) начал удаление команды {}", chatId, team.getId());
        bot.setUserState(chatId, UserState.TEAM_WAITING_DELETE);

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

        // Удалим данные о задачах
        taskRepository.deleteByTeam(team);
        // Удалим данные об участниках
        teamMemberRepository.deleteByTeam(team);
        // Удалим команду
        teamRepository.delete(team);

        log.info("Команда удалена: {}", team);
        bot.sendMessage(chatId, "Команда удалена! \n" +
                "Посмотреть список команд можно в меню команд \"Мои команды\"");
        bot.deleteUserState(chatId);
    }
}
