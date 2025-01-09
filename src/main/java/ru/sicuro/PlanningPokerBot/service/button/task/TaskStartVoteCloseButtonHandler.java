package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.PlanningSession;
import ru.sicuro.PlanningPokerBot.model.PlanningSessionStatus;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.TeamMember;
import ru.sicuro.PlanningPokerBot.reposirory.PlanningSessionRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamMemberRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class TaskStartVoteCloseButtonHandler implements ButtonHandler {
    private final TeamRepository teamRepository;
    private final PlanningSessionRepository planningSessionRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public String getCallbackData() {
        return "TASK_START_VOTE_CLOSE_BUTTON";
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

        // Получим команду
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена!"));
        // Получим сессию
        List<PlanningSession> planningSessions = planningSessionRepository.findByTeamAndStatus(team, PlanningSessionStatus.ACTIVE);
        planningSessions.forEach(session -> {
            session.setCompletedAt(LocalDateTime.now());
            session.setStatus(PlanningSessionStatus.COMPLETED);
        });
        planningSessionRepository.saveAll(planningSessions);

        message.setText("Голосование закрыто!");

        // Отправка сообщения
        bot.sendMessage(message);
        log.info("Пользователь({}) закончил голосование",chatId);

        // Оповестим команду об окончании голосования
        List<TeamMember> teamMembers = teamMemberRepository.findByTeam(team);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format("Пользователь %s(%s) закончил голосование для команды \"%s\"",
                team.getCreatedBy().getFullName(),
                team.getCreatedBy().getUsername(),
                team.getName()));
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setParseMode(ParseMode.HTML);

        for (TeamMember teamMember : teamMembers) {
            sendMessage.setChatId(teamMember.getUser().getChatId());
            bot.sendMessage(sendMessage);
        }

    }
}
