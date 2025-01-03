package ru.sicuro.PlanningPokerBot.service.button;

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
        var optionalTeam = teamRepository.findById(Long.valueOf(teamId));
        Team team;
        if (optionalTeam.isEmpty()) {
            log.warn("Команда не найдена по id {}", teamId);
            return;
        } else {
            team = optionalTeam.get();
        }

        // Данные пользователя
        var optionalUser = userRepository.findByChatId(chatId);
        User user;
        if (optionalUser.isEmpty()) {
            log.warn("Пользователь не найден по chatId {}!", chatId);
            return;
        } else {
            user = optionalUser.get();
        }

        // Получим список пользователей не в команде
        var users = userRepository.findUsersNotInTeamAndNotCurrentUser(team.getId(), user.getId());

        // Сформируем кнопки для приглашения пользователей
        message.setText("Выберите пользователей для добавления в команду:");
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
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
    }
}
