package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.model.Task;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.TaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.UserState;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;
import ru.sicuro.PlanningPokerBot.service.StepHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@AllArgsConstructor
public class TaskAddStepButtonHandler implements ButtonHandler, StepHandler {

    private static final Map<Long, Task> tempStorage = new ConcurrentHashMap<>();
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    public String getCallbackData() {
        return "TASK_ADD_STEP_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String teamId = callbackQuery.split(" ")[1];

        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена!"));
        User user = userRepository.findByChatId(chatId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Введите название задачи:");

        bot.sendMessage(message);
        log.info("Пользователь({}) начал добавление задачи для команды({})",
                user.getChatId(),
                team.getName());

        Task task = new Task();
        task.setTeam(team);
        tempStorage.put(chatId, task);
        bot.setUserState(chatId, UserState.ADDING_TASK_NAME);
    }


    @Override
    public void handleStep(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();
        var state = bot.getUserState(chatId);
        String messageText = update.getMessage().getText();

        // Если пользователь не начинал создание команды выходим
        if (state == UserState.ADDING_TASK_NAME) {
            // Сохраняем название задачи
            Task task = tempStorage.get(chatId);
            task.setTitle(messageText);

            bot.sendMessage(chatId, "Введите описание задачи (или отправьте \"-\", чтобы пропустить):");
            bot.setUserState(chatId, UserState.ADDING_TASK_DESCRIPTION);

        } else if (state == UserState.ADDING_TASK_DESCRIPTION) {
            // Сохраняем описание задачи
            Task task = tempStorage.get(chatId);
            task.setDescription(messageText.equals("-") ? null : messageText); // если отправили - то не добавляем описание

            bot.sendMessage(chatId, "Введите ссылку на задачу (или отправьте \"-\", чтобы пропустить):");
            bot.setUserState(chatId, UserState.ADDING_TASK_LINK);

        } else if (state == UserState.ADDING_TASK_LINK) {
            // Сохраняем ссылку задачи временно
            Task task = tempStorage.get(chatId);
            task.setLink(messageText.equals("-") ? null : messageText); // если отправили - то не добавляем ссылку
            task.setCreatedAt(LocalDateTime.now());

            taskRepository.save(task);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("🎯Задача успешно добавлена!\n" +
                    "Добавить ещё?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInLine = getListsButton(task.getTeam().getId());

            // Добавляем кнопки
            markup.setKeyboard(rowsInLine);
            message.setReplyMarkup(markup);

            bot.sendMessage(message);
            bot.deleteUserState(chatId);
            tempStorage.remove(chatId);
            log.info("Новая задача создана: {}", task);
        }
    }

    private static List<List<InlineKeyboardButton>> getListsButton(Long chatId) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        // Кнопка добавление команды
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("ДА");
        button.setCallbackData(String.format("TASK_ADD_STEP_BUTTON %s", chatId));
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        return rowsInLine;
    }
}
