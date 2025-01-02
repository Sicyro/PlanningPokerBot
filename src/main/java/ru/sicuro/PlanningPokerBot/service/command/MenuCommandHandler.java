package ru.sicuro.PlanningPokerBot.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MenuCommandHandler implements CommandHandler {

    @Override
    public String getCommandName() {
        return "/menu";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();

        // Формируем ответное сообщение
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Доступные команды меню:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        // Кнопка регистрации
        var registerButton = new InlineKeyboardButton();
        registerButton.setText("Регистрация");
        registerButton.setCallbackData("REGISTER_BUTTON");
        rowInLine.add(registerButton);

        // Добавляем линию кнопок
        rowsInLine.add(rowInLine);

        // Добавляем кнопки
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
    }
}