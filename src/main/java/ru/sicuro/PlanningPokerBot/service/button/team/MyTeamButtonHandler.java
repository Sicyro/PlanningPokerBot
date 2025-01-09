package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.reposirory.TeamMemberRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MyTeamButtonHandler implements ButtonHandler {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAM_BUTTON";
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
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));
        // Количество участников в команде
        var teamMembers = teamMemberRepository.findByTeam(team);

        // Строка вывода
        String text = String.format("""
                Команда: ⚔️%s
                
                📅Дата создания: %s
                👨‍👩‍👧‍👦Количество участников: %d
               
                Выберите действие над командой:
                """,
                team.getName(),
                team.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                teamMembers.size());

        message.setText(text);

        // Добавим кнопки
        List<List<InlineKeyboardButton>> rowsInLine = getListsButton(team.getId());
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        bot.sendMessage(message);

    }

    private static List<List<InlineKeyboardButton>> getListsButton(Long teamId) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        // Изменить имя команды
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("Переименовать");
        button.setCallbackData("MY_TEAM_RENAME_BUTTON " + teamId);
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        // Список участников
        rowInLine = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Список участников");
        button.setCallbackData("MY_TEAM_GET_MEMBER_BUTTON " + teamId);
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        // Добавить участника
        rowInLine = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Добавить участников");
        button.setCallbackData("MY_TEAM_ADD_MEMBER_BUTTON " + teamId);
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        // Удалить команду
        rowInLine = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Удалить");
        button.setCallbackData("MY_TEAM_DELETE_BUTTON " + teamId);
        rowInLine.add(button);
        rowsInLine.add(rowInLine);

        return rowsInLine;
    }
}
