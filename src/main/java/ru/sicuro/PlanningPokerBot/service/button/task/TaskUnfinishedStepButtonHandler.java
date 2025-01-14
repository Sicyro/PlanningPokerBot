package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.model.Task;
import ru.sicuro.PlanningPokerBot.model.TaskState;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.reposirory.TaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TaskUnfinishedStepButtonHandler implements ButtonHandler {

    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;

    @Override
    public String getCallbackData() {
        return "TASK_UNFINISHED_STEP_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String teamId = callbackQuery.split(" ")[1];

        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена!"));

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setDisableWebPagePreview(true);
        message.setParseMode(ParseMode.HTML);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Список незавершённых задач для команды <b>")
                .append(team.getViewHtml())
                .append("</b>:")
                .append("\n\n");

        List<Task> tasks = taskRepository.findByTeamAndStatus(team, TaskState.PENDING);

        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                stringBuilder
                        .append(task.getViewHtml())
                        .append("\n");
            }
        } else {
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder
                    .append("У вас нет незавершённых задач")
                    .append("\n")
                    .append("Создать новую задачу можно в меню задач");
        }

        message.setText(stringBuilder.toString());

        bot.sendMessage(message);
    }
}
