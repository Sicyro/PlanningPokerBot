package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.SessionTask;
import ru.sicuro.PlanningPokerBot.model.Task;
import ru.sicuro.PlanningPokerBot.model.TaskVote;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.SessionTaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TaskVoteRepository;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class TaskStartVoteVotedButtonHandler implements ButtonHandler {


    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskVoteRepository taskVoteRepository;
    private final SessionTaskRepository sessionTaskRepository;

    @Override
    public String getCallbackData() {
        return "TASK_START_VOTE_VOTED_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String vote = callbackQuery.split(" ")[1];
        String taskId = callbackQuery.split(" ")[2];

        Task task = taskRepository.findById(Long.valueOf(taskId)).orElseThrow(() -> new IllegalArgumentException("Задача не найдена!"));
        User user = userRepository.findByChatId(chatId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);

        // Проверим открыта ли задача для голосования
        List<SessionTask> sessionTasks = sessionTaskRepository.findByTask(task);

        // Если нет, то прерываем голосование
        if (sessionTasks.isEmpty()) {
            message.setText("Голосование за задачу закрыто! " + task.getView());
            bot.sendMessage(message);
            return;
        }

        message.setText("Ваш голос учтён! 🎯" + task.getTitle());

        // Запишем результат голоса
        TaskVote taskVote = new TaskVote();
        taskVote.setTask(task);
        taskVote.setUser(user);
        taskVote.setVote(Integer.valueOf(vote));

        taskVoteRepository.save(taskVote);

        bot.sendMessage(message);
        log.info("Пользователь({}) проголосовал за задачу {}", chatId, taskVote);
    }
}
