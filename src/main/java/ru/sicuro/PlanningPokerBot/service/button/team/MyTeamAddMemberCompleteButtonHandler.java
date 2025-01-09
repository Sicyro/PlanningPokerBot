package ru.sicuro.PlanningPokerBot.service.button.team;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.model.Team;
import ru.sicuro.PlanningPokerBot.model.TeamMember;
import ru.sicuro.PlanningPokerBot.model.TeamMemberId;
import ru.sicuro.PlanningPokerBot.model.User;
import ru.sicuro.PlanningPokerBot.reposirory.TeamMemberRepository;
import ru.sicuro.PlanningPokerBot.reposirory.TeamRepository;
import ru.sicuro.PlanningPokerBot.reposirory.UserRepository;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;
import ru.sicuro.PlanningPokerBot.service.button.ButtonHandler;

@Slf4j
@Service
@AllArgsConstructor
public class MyTeamAddMemberCompleteButtonHandler implements ButtonHandler {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Override
    public String getCallbackData() {
        return "MY_TEAM_ADD_MEMBER_COMPLETE_BUTTON";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQuery = update.getCallbackQuery().getData();
        String teamId = callbackQuery.split(" ")[1];
        String result = callbackQuery.split(" ")[2];

        // Класс для работы с текстом которы был уже передан
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);

        // Получим данные для работы с командой
        Team team = teamRepository.findById(Long.valueOf(teamId)).orElseThrow(() -> new IllegalArgumentException("Команда не найдена!"));
        // Данные пользователя
        User user = userRepository.findByChatId(chatId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

        if (result.equals("0")) {
            // Если не согласился
            message.setText("❌Приглашение отклонено!");
            // Сообщение для отправителя
            bot.sendMessage(team.getCreatedBy().getChatId(),
                    String.format("❌Пользователь (%s(%s)) отклонил приглашение!",
                            user.getFullName(),
                            user.getUsername()));
            bot.sendMessage(message);
            log.info("Пользователь ({}) отклонил приглашение!", user.getChatId());
            return;
        } else if (result.equals("1")) {
            message.setText("✅Приглашение принято!");
        }

        // Получаем список участников
        var teamMembers = teamMemberRepository.findByTeam(team);

        // Добавим пользователя в команду
        TeamMemberId teamMemberId = new TeamMemberId();
        teamMemberId.setUserId(user.getId());
        teamMemberId.setTeamId(team.getId());

        TeamMember teamMember = new TeamMember();
        teamMember.setId(teamMemberId);
        teamMember.setTeam(team);
        teamMember.setUser(user);

        teamMemberRepository.save(teamMember);

        // Обновим текст
        bot.sendMessage(message);
        // Сообщение для отправителя
        bot.sendMessage(team.getCreatedBy().getChatId(),
                String.format("✅Пользователь (%s(%s)) принял приглашение!",
                        user.getFullName(),
                        user.getUsername()));
        log.info("Пользователь ({}) принял приглашение в команду \"{}\"",
                user.getChatId(),
                team.getName());
    }
}
