package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.TeamMember;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.TeamMemberRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

@Slf4j
@Service
@AllArgsConstructor
public class MyTeamGetMemberButtonHandler implements ButtonHandler {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAM_GET_MEMBER_BUTTON";
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

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Список участников команды:")
                .append("\n")
                .append("\n");

        var teamMembers = teamMemberRepository.findByTeam(team);
        for (TeamMember teamMember : teamMembers) {
            User user = teamMember.getUser();
            stringBuilder
                    .append("⭐")
                    .append(user.getFullName())
                    .append("(")
                    .append(user.getUsername())
                    .append(")\n");
        }

        message.setText(stringBuilder.toString());
        bot.sendMessage(message);

    }
}
