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
public class MyTeamsButtonHandler implements ButtonHandler {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAMS_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Список ваших команд:")
                .append("\n")
                .append("\n");

        // Получим пользователя
        var queueUser = userRepository.findByChatId(chatId);
        User user;
        if (queueUser.isPresent()) {
            user = queueUser.get();
        } else {
            log.warn("Пользователь не найден!");
            bot.sendMessage(chatId,"❗Пользователь не найден! Попробуйте зарегистрироваться заново используя команду /register");
            return;
        }

        // Получим список команд и сформируем кнопки
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        var teams = teamRepository.findByCreatedBy(user);

        // Сортируем по id
        teams.sort(((team1, team2) -> Long.compare(team1.getId(), team2.getId())));

        for (Team team : teams) {
            // Сформируем текст
            stringBuilder
                    .append("⚔️")
                    .append(team.getName())
                    .append("\n");

            // Сформируем кнопки
            List<InlineKeyboardButton> rowInLineRegister = new ArrayList<>();
            var MyTeamButton = new InlineKeyboardButton();
            MyTeamButton.setText(team.getName());
            MyTeamButton.setCallbackData(String.format("MY_TEAM_BUTTON %s", team.getId()));
            rowInLineRegister.add(MyTeamButton);
            rowsInLine.add(rowInLineRegister);
        }

        stringBuilder
                .append("\n")
                .append("Выберите команду:");

        // Установим текст
        message.setText(stringBuilder.toString());

        // Установим кнопки
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
    }
}
