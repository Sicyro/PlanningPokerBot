package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MyTeamAddMemberButtonHandler implements ButtonHandler {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAM_ADD_MEMBER_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String teamId = callbackQuery.split(" ")[1];

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);

        // Получим данные для работы с командой
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена!"));

        // Данные пользователя
        User user = userRepository.findById(chatId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

        // Получим список пользователей не в команде
        var users = userRepository.findUsersNotInTeamAndNotCurrentUser(team, user);

        // Сформируем кнопки для приглашения пользователей
        message.setText("Выберите пользователей для добавления в команду:");
        InlineKeyboardMarkup markup = getInlineKeyboardMarkup(users, team);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkup(List<User> users, Team team) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (User user1 : users) {
            // Сформируем кнопки
            List<InlineKeyboardButton> rowInLine = new ArrayList<>();
            var button = new InlineKeyboardButton();
            button.setText(user1.getFullName());
            // Вместе с кнопкой добавляем id пользователя и команды для дальнейшего парсинга
            button.setCallbackData(String.format("MY_TEAM_ADD_MEMBER_STEP_BUTTON %s %s",
                    user1.getId(),
                    team.getId()));
            rowInLine.add(button);
            rowsInLine.add(rowInLine);
        }

        // Установим кнопки
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);
        return markup;
    }
}
