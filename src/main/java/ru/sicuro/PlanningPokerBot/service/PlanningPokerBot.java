package ru.sicuro.PlanningPokerBot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sicuro.PlanningPokerBot.config.BotConfig;
import ru.sicuro.PlanningPokerBot.model.RegistrationState;
import ru.sicuro.PlanningPokerBot.model.Role;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;
import ru.sicuro.PlanningPokerBot.service.button.RegisterButtonHandler;
import ru.sicuro.PlanningPokerBot.service.command.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
public class PlanningPokerBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final UserRepository userRepository;

    // Команды бота
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private final RegisterCommandHandler registerCommandHandler;

    // Кнопки бота
    private final Map<String, ButtonHandler> buttonHandler = new HashMap<>();

    public PlanningPokerBot(BotConfig config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;

        // Обработчики кнопок бота
        buttonHandler.put("REGISTER_BUTTON", new RegisterButtonHandler(this.userRepository));

        //Добавим список команд для обработки
        registerCommandHandler = new RegisterCommandHandler(this.userRepository);

        commandHandlers.put("/start", new StartCommandHandler(this.userRepository));
        commandHandlers.put("/help", new HelpCommandHandler());
        commandHandlers.put("/menu", new MenuCommandHandler());
        commandHandlers.put(registerCommandHandler.getCommandName(), registerCommandHandler);

        // Команды для меню
        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/menu", "Вызов главного меню"));
        listCommands.add(new BotCommand("/help", "Помощь"));
        listCommands.add(new BotCommand("/register", "Регистрация"));
        try {
            this.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
           log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Обработка команд и текста
            String messageText = update.getMessage().getText();
            String command = messageText.split(" ")[0]; // берём первую команду

            CommandHandler handler = commandHandlers.get(command);
            if (handler != null) {
                handler.handle(update, this);
            } else {
                registerCommandHandler.handleRegistrationStep(update, this);
            }
        } else if (update.hasCallbackQuery()) {
            // Обработка кнопок
            String callbackQuery = update.getCallbackQuery().getData();

            ButtonHandler handler = buttonHandler.get(callbackQuery);
            if (handler != null) {
                handler.handle(update, this);
            }
        }
    }

    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        sendMessage(sendMessage);
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}