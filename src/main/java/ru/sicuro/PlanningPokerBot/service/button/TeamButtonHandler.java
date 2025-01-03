package ru.sicuro.PlanningPokerBot.service.button;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TeamButtonHandler implements ButtonHandler {

    @Override
    public String getCallbackData() {
        return "TEAM_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Управление командами:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = getListsButton();

        // Добавляем кнопки
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
    }

    private static List<List<InlineKeyboardButton>> getListsButton() {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        // Кнопка отображения моих команд
        List<InlineKeyboardButton> rowInLineCreateTeam = new ArrayList<>();
        var createTeamButton = new InlineKeyboardButton();
        createTeamButton.setText("Мои команды");
        createTeamButton.setCallbackData("MY_TEAMS_BUTTON");
        rowInLineCreateTeam.add(createTeamButton);
        rowsInLine.add(rowInLineCreateTeam);

        // Кнопка создания команды
        List<InlineKeyboardButton> rowInLineRegister = new ArrayList<>();
        var registerButton = new InlineKeyboardButton();
        registerButton.setText("Создать команду");
        registerButton.setCallbackData("CREATE_TEAM_BUTTON");
        rowInLineRegister.add(registerButton);
        rowsInLine.add(rowInLineRegister);

        return rowsInLine;
    }
}
