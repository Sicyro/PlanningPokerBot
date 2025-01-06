package ru.sicuro.PlanningPokerBot.service;

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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sicuro.PlanningPokerBot.config.BotConfig;
import ru.sicuro.PlanningPokerBot.reposirory.TaskRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamMemberRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.button.*;
import ru.sicuro.PlanningPokerBot.service.button.task.TaskAddButtonHandler;
import ru.sicuro.PlanningPokerBot.service.button.task.TaskAddStepButtonHandler;
import ru.sicuro.PlanningPokerBot.service.button.task.TaskButtonHandler;
import ru.sicuro.PlanningPokerBot.service.button.team.*;
import ru.sicuro.PlanningPokerBot.service.command.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PlanningPokerBot extends TelegramLongPollingBot {

    private final BotConfig config;

    // Состояние пользователя
    // Переменная необходима для определия действия пользователя
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    // Команды бота
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    // Кнопки бота
    @Getter
    private final Map<String, ButtonHandler> buttonHandler = new HashMap<>();
    private final List<StepHandler> buttonStepHandlers = new ArrayList<>();

    /**
     * @param config Конфигурация бота
     * @param userRepository Класс для работы с БД, таблица пользователи
     */
    public PlanningPokerBot(BotConfig config,
                            UserRepository userRepository,
                            TeamRepository teamRepository,
                            TeamMemberRepository teamMemberRepository,
                            TaskRepository taskRepository) {
        this.config = config;

        // Обработчики кнопок бота
        TeamCreateButtonHandler createTeamButtonHandler = new TeamCreateButtonHandler(teamRepository, userRepository);
        MyTeamRenameButtonHandler myTeamRenameButtonHandler = new MyTeamRenameButtonHandler(teamRepository);
        MyTeamDeleteButtonHandler myTeamDeleteButtonHandler = new MyTeamDeleteButtonHandler(teamRepository, teamMemberRepository);
        TaskAddStepButtonHandler taskAddStepButtonHandler = new TaskAddStepButtonHandler(teamRepository,
                userRepository,
                taskRepository);

        // Нажатие кнопки
        buttonHandler.put("REGISTER_BUTTON", new RegisterButtonHandler(userRepository));
        buttonHandler.put(createTeamButtonHandler.getCallbackData(), createTeamButtonHandler);
        buttonHandler.put("TEAM_BUTTON", new TeamButtonHandler());
        buttonHandler.put("MY_TEAMS_BUTTON", new MyTeamsButtonHandler(teamRepository, userRepository));
        buttonHandler.put("MY_TEAM_BUTTON", new MyTeamButtonHandler(teamRepository, teamMemberRepository));
        buttonHandler.put(myTeamRenameButtonHandler.getCallbackData(), myTeamRenameButtonHandler);
        buttonHandler.put(myTeamDeleteButtonHandler.getCallbackData(), myTeamDeleteButtonHandler);
        buttonHandler.put("MY_TEAM_GET_MEMBER_BUTTON", new MyTeamGetMemberButtonHandler(teamRepository, teamMemberRepository));
        buttonHandler.put("MY_TEAM_ADD_MEMBER_BUTTON",
                new MyTeamAddMemberButtonHandler(teamRepository, userRepository));
        buttonHandler.put("MY_TEAM_ADD_MEMBER_STEP_BUTTON",
                new MyTeamAddMemberStepButtonHandler(teamRepository, userRepository));
        buttonHandler.put("MY_TEAM_ADD_MEMBER_COMPLETE_BUTTON",
                new MyTeamAddMemberCompleteButtonHandler(teamRepository, teamMemberRepository, userRepository));
        buttonHandler.put("TASK_BUTTON", new TaskButtonHandler());
        buttonHandler.put("TASK_ADD_BUTTON", new TaskAddButtonHandler(userRepository, teamRepository));
        buttonHandler.put(taskAddStepButtonHandler.getCallbackData(), taskAddStepButtonHandler);

        //Добавим список команд для обработки
        RegisterCommandHandler registerCommandHandler = new RegisterCommandHandler(userRepository);

        commandHandlers.put("/start", new StartCommandHandler(userRepository));
        commandHandlers.put("/help", new HelpCommandHandler());
        commandHandlers.put("/menu", new MenuCommandHandler());
        commandHandlers.put(registerCommandHandler.getCommandName(), registerCommandHandler);

        // Действие после ввода текста:
        buttonStepHandlers.add(registerCommandHandler);
        buttonStepHandlers.add(createTeamButtonHandler);
        buttonStepHandlers.add(myTeamRenameButtonHandler);
        buttonStepHandlers.add(myTeamDeleteButtonHandler);
        buttonStepHandlers.add(taskAddStepButtonHandler);

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
                for (StepHandler stepHandler : buttonStepHandlers) {
                    stepHandler.handleStep(update, this);
                }
            }
        } else if (update.hasCallbackQuery()) {
            // Обработка кнопок
            String callbackQuery = update.getCallbackQuery().getData();
            callbackQuery = callbackQuery.split(" ")[0];

            ButtonHandler handler = buttonHandler.get(callbackQuery);
            if (handler != null) {
                try {
                    handler.handle(update, this);
                } catch (IllegalStateException e) {
                   log.error(e.getMessage(), e);
                }
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

    public UserState getUserState(Long chatId) {
        return userStates.get(chatId);
    }

    // Устанавливаем статус пользователя
    public void setUserState(long chatId, UserState state) {
        log.debug("Пользователю({}) установили статус \"{}\"", chatId, state);
        userStates.put(chatId, state);
    }

    // Удаляем статус пользователя
    public void deleteUserState(long chatId) {
        log.debug("Пользователю({}) очистили статус", chatId);
        userStates.remove(chatId);
    }
}