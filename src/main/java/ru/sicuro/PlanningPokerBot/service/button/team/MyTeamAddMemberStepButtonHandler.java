package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
public class MyTeamAddMemberStepButtonHandler implements ButtonHandler {

    private TeamRepository teamRepository;
    private UserRepository userRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAM_ADD_MEMBER_STEP_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackQuery = update.getCallbackQuery().getData();
        String userId = callbackQuery.split(" ")[1];
        String teamId = callbackQuery.split(" ")[2];

        // Получим данные для работы с командой
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена!"));
        // Данные пользователя
        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

        String text = String.format("""
                ⚡Вас приглашают в команду ⚔️%s
                Принять приглашение?
                """,
                team.getName());

        // Формируем сообщение для пользователя
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = getListsButton(team.getId());

        // Добавляем кнопки
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);
        bot.sendMessage(chatId, String.format("⚡Приглашение для пользователя (%s(%s)) в команду \"%s\" отправлено",
                user.getFullName(),
                user.getUsername(),
                team.getName()));
        log.info("Приглашение для пользователя({}) в команду {}({}) отправлено",
                user.getChatId(),
                team.getName(),
                team.getId());
    }

    private List<List<InlineKeyboardButton>> getListsButton(Long teamId) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        // Кнопка ДА
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("Да");
        // Добавим id команды и результат ответа (0 - Нет, 1 - Да)
        button.setCallbackData(String.format("MY_TEAM_ADD_MEMBER_COMPLETE_BUTTON %s %d",
                teamId,
                1));
        rowInLine.add(button);

        // Кнопка НЕТ
        button = new InlineKeyboardButton();
        button.setText("Нет");
        // Добавим id команды и результат ответа (0 - Нет, 1 - Да)
        button.setCallbackData(String.format("MY_TEAM_ADD_MEMBER_COMPLETE_BUTTON %s %d",
                teamId,
                0));
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        return rowsInLine;
    }
}
