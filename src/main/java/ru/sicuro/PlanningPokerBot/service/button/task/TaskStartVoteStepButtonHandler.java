package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.model.*;
import ru.sicuro.PlanningPokerBot.reposirory.PlanningSessionRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamMemberRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class TaskStartVoteStepButtonHandler implements ButtonHandler {
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PlanningSessionRepository planningSessionRepository;

    @Override
    public String getCallbackData() {
        return "TASK_START_VOTE_STEP_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String teamId = callbackQuery.split(" ")[1];

        // Удалим статус для пользователя
        bot.deleteUserState(chatId);

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setDisableWebPagePreview(true);
        message.setParseMode(ParseMode.HTML);

        // Получим команду
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена!"));
        // Получим задачи команды
        List<Task> tasks = taskRepository.findByTeamAndStatus(team, TaskState.PENDING);

        // Проверим нет ли активной сессии
        boolean planningSessionIsActive = false;
        List<PlanningSession> planningSessions = planningSessionRepository.findByTeamAndStatus(team, PlanningSessionStatus.ACTIVE);
        PlanningSession planningSession = new PlanningSession();
        if (planningSessions.isEmpty()) {
            planningSession.setTeam(team);
            planningSessionRepository.save(planningSession);
        } else {
            // Вернём в ложь чтобы не отправлять сообщения о начале голосования
            planningSession = planningSessions.get(0);
            planningSessionIsActive = true;
        }

        String messageForMembers = String.format("Пользователь %s(%s) начал голосование для команды \"%s\" \n",
                team.getCreatedBy().getFullName(),
                team.getCreatedBy().getUsername(),
                team.getName());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Список задач для команды <b>⚔️")
                .append(bot.escapeHtml(team.getName()))
                .append("</b>:")
                .append("\n\n");

        // Сортируем по id
        tasks.sort(((tasks1, tasks2) -> Long.compare(tasks1.getId(), tasks2.getId())));

        // Переменная для кнопок
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        boolean started = false;

        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                // Сформируем текст
                if (task.getLink() != null) {
                    stringBuilder
                            .append("🎯<a href='")
                            .append(bot.escapeHtml(task.getLink()))
                            .append("'>")
                            .append(bot.escapeHtml(task.getTitle()))
                            .append("</a>")
                            .append("\n");
                } else {
                    stringBuilder
                            .append("🎯")
                            .append(bot.escapeHtml(task.getTitle()))
                            .append("\n");
                }

                // Сформируем кнопки
                List<InlineKeyboardButton> rowInLine = new ArrayList<>();
                var button = new InlineKeyboardButton();
                button.setText(task.getTitle());
                button.setCallbackData(String.format("TASK_START_VOTE_STEP_TWO_BUTTON %s %s",
                        task.getId(),
                        planningSession.getId()));
                rowInLine.add(button);
                rowsInLine.add(rowInLine);
            }

            messageForMembers += stringBuilder.toString();
            stringBuilder
                    .append("\n")
                    .append("Выберите задачу:");
            started = true;
        } else {
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder
                    .append("У вас нет незавершённых задач!");
        }

        // Сформируем кнопку закрытия голосования
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("❌Закрыть голосование");
        button.setCallbackData(String.format("TASK_START_VOTE_CLOSE_BUTTON %s",
                team.getId()));
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        // Установим текст
        message.setText(stringBuilder.toString());

        // Установим кнопки
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        // Отправка сообщения
        bot.sendMessage(message);
        log.info("Пользователь({}) начал голосование", chatId);

        if (!started || planningSessionIsActive) {
            // Если нет задач, то не оповещаем пользователей
            return;
        }

        // Оповестим команду о начале голосования
        List<TeamMember> teamMembers = teamMemberRepository.findByTeam(team);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageForMembers);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setParseMode(ParseMode.HTML);

        for (TeamMember teamMember : teamMembers) {
            sendMessage.setChatId(teamMember.getUser().getChatId());
            bot.sendMessage(sendMessage);
        }
    }
}
