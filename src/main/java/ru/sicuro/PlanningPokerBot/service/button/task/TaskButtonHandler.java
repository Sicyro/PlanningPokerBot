package ru.sicuro.PlanningPokerBot.service.button.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TaskButtonHandler implements ButtonHandler {
    @Override
    public String getCallbackData() {
        return "TASK_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("🎯Управление задачами:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = getListsButton();

        // Добавляем кнопки
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
    }

    private static List<List<InlineKeyboardButton>> getListsButton() {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        // Добавить задачи
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("Добавить задачи");
        button.setCallbackData("TASK_ADD_BUTTON");
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        // Начать голосование за незавершённые задачи
        rowInLine = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Просмотр незавершённых задач");
        button.setCallbackData("TASK_UNFINISHED_BUTTON");
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        // Начать голосование
        rowInLine = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Начать голосование");
        button.setCallbackData("TASK_START_VOTE_BUTTON");
        rowInLine.add(button);
        rowsInLine.add(rowInLine);


        return rowsInLine;
    }
}
