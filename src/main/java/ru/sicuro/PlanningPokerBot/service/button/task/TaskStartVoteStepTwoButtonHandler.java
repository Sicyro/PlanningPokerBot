package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.model.*;
import ru.sicuro.PlanningPokerBot.reposirory.*;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class TaskStartVoteStepTwoButtonHandler implements ButtonHandler {
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PlanningSessionRepository planningSessionRepository;
    private final SessionTaskRepository sessionTaskRepository;

    @Override
    public String getCallbackData() {
        return "TASK_START_VOTE_STEP_TWO_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String taskId = callbackQuery.split(" ")[1];
        String sessionId = callbackQuery.split(" ")[2];

        // Получим задачу
        Task task = taskRepository.findById(Long.valueOf(taskId)).orElseThrow(() -> new IllegalArgumentException("Задача не найдена!"));
        PlanningSession planningSession = planningSessionRepository.findById(
                Long.valueOf(sessionId)).orElseThrow(() -> new IllegalArgumentException("Сессия не найдена!"));

        // Разблокируем задачу для голосования
        SessionTaskId sessionTaskId = new SessionTaskId();
        sessionTaskId.setTaskId(task.getId());
        sessionTaskId.setSessionId(planningSession.getId());

        SessionTask sessionTask = new SessionTask();
        sessionTask.setId(sessionTaskId);
        sessionTask.setTask(task);
        sessionTask.setSession(planningSession);

        sessionTaskRepository.save(sessionTask);

        String messageForMembers = String.format("""
                Голосование за задачу: %s
                
                Описание: %s""",
                task.getViewHtml(),
                task.getDescriptionHtml());

        // Класс для работы с текстом который был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setParseMode(ParseMode.HTML);
        message.setDisableWebPagePreview(true);
        message.setText(messageForMembers);

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        // Сформируем кнопки
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("Завершить голосование");
        button.setCallbackData(String.format("TASK_START_VOTE_COMPLETE_BUTTON %s",
                task.getId()));
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        // Установим кнопки
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
        log.info("Пользователь({}) начал голосование за задачу {}", chatId, task);

        // Оповестим команду о начале голосования за задачу

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageForMembers);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.setReplyMarkup(getKeyboardMarkupVote(task));

        List<TeamMember> teamMembers = teamMemberRepository.findByTeam(task.getTeam());
        for (TeamMember teamMember : teamMembers) {
            sendMessage.setChatId(teamMember.getUser().getChatId());
            bot.sendMessage(sendMessage);
        }
    }

    private InlineKeyboardMarkup getKeyboardMarkupVote(Task task) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        // 1
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("1 (3 дня)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 1 %s", task.getId()));
        rowInLine.add(button);

        // 2
        button = new InlineKeyboardButton();
        button.setText("2 (<1 спринта)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 2 %s", task.getId()));
        rowInLine.add(button);

        // Завершим строку
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();

        // 3
        button = new InlineKeyboardButton();
        button.setText("3 (1 спринт)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 3 %s", task.getId()));
        rowInLine.add(button);


        // 5
        button = new InlineKeyboardButton();
        button.setText("5 (1.5 спринта)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 5 %s", task.getId()));
        rowInLine.add(button);

        // Завершим строку
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();

        // 8
        button = new InlineKeyboardButton();
        button.setText("8 (2 спринта)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 8 %s", task.getId()));
        rowInLine.add(button);

        //13
        button = new InlineKeyboardButton();
        button.setText("13 (3 спринта)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 13 %s", task.getId()));
        rowInLine.add(button);

        // Завершим строку
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();

        //21
        button = new InlineKeyboardButton();
        button.setText("21 (5 спринтов)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 21 %s", task.getId()));
        rowInLine.add(button);

        //34
        button = new InlineKeyboardButton();
        button.setText("34 (8 спринтов)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 34 %s", task.getId()));
        rowInLine.add(button);

        // Завершим строку
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();

        //55
        button = new InlineKeyboardButton();
        button.setText("55 (10 спринтов)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 55 %s", task.getId()));
        rowInLine.add(button);

        //89
        button = new InlineKeyboardButton();
        button.setText("89 (>8 спринтов)");
        button.setCallbackData(String.format("TASK_START_VOTE_VOTED_BUTTON 89 %s", task.getId()));
        rowInLine.add(button);

        // Завершим строку
        rowsInLine.add(rowInLine);

        markup.setKeyboard(rowsInLine);

        return markup;
    }
}
