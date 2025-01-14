package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.model.*;
import ru.sicuro.PlanningPokerBot.reposirory.PlanningSessionRepository;
import ru.sicuro.PlanningPokerBot.reposirory.SessionTaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TaskVoteRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.StepHandler;
import ru.sicuro.PlanningPokerBot.service.UserState;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@AllArgsConstructor
@Service
public class TaskStartVoteCompleteButtonHandler implements ButtonHandler, StepHandler {
    private final TaskRepository taskRepository;
    private final TaskVoteRepository taskVoteRepository;
    private final SessionTaskRepository sessionTaskRepository;

    private final Map<Long, Task> tempStorage = new ConcurrentHashMap<>();

    @Override
    public String getCallbackData() {
        return "TASK_START_VOTE_COMPLETE_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String taskId = callbackQuery.split(" ")[1];

        Task task = taskRepository.findById(Long.valueOf(taskId)).orElseThrow(() -> new IllegalArgumentException("Задача не найдена!"));
        Double finalEstimate = taskVoteRepository.findAverageVoteByTask(task);
        if (finalEstimate != null) {
            task.setFinalEstimate(findClosest(finalEstimate));
            task.setCompletedAt(LocalDateTime.now());
            task.setStatus(TaskState.COMPLETED);
            taskRepository.save(task);
        }

        // Закроем задачу для голосования
        List<SessionTask> sessionTasks = sessionTaskRepository.findByTask(task);
        sessionTaskRepository.deleteAll(sessionTasks);

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setDisableWebPagePreview(true);
        message.setParseMode(ParseMode.HTML);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Результат голосования за задачу ")
                .append(task.getView())
                .append(":\n")
                .append("Средняя оценка: ")
                .append(task.getFinalEstimate())
                .append("\n")
                .append("\n")
                .append("Результаты голосования:")
                .append("\n");

        List<TaskVote> taskVotes = taskVoteRepository.findByTask(task);

        taskVotes.forEach(taskVote -> {
            stringBuilder
                    .append(taskVote.getUser().getView())
                    .append(": <b>")
                    .append(taskVote.getVote())
                    .append("</b>\n");
        });

        stringBuilder
                .append("\n")
                .append("Чтобы изменить оценку, введите новую:");
        bot.setUserState(chatId, UserState.TASK_WAITING_VOTE);
        tempStorage.put(chatId, task);

        // Сформируем кнопки
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("<< Назад к задачам");
        button.setCallbackData(String.format("TASK_START_VOTE_STEP_BUTTON %s",
                task.getTeam().getId()));
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        message.setText(stringBuilder.toString());
        bot.sendMessage(message);
        log.info("Пользователь({}) завершил голосование за задачу {}", chatId, task);
    }

    public static Integer findClosest(Double input) {
        final List<Integer> NUMBERS = List.of(1, 2, 3, 5, 8, 13, 21, 34, 55, 89);

        int closest = NUMBERS.get(0);
        double minDifference = Math.abs(input - closest);

        for (int number : NUMBERS) {
            double difference = Math.abs(input - number);
            if (difference < minDifference) {
                closest = number;
                minDifference = difference;
            }
        }

        return closest;
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

        if (!UserState.TASK_WAITING_VOTE.equals(state)) {
            return;
        }

        // Изменим оценку
        Task task = tempStorage.get(chatId);
        task.setFinalEstimate(Integer.valueOf(messageText));

        taskRepository.save(task);

        bot.sendMessage(chatId, "Итоговая оценка изменена!");
        bot.deleteUserState(chatId);
        tempStorage.remove(chatId);
        log.info("Итоговая оценка изменена в задаче: {}", task);
    }
}
