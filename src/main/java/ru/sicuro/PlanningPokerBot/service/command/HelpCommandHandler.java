package ru.sicuro.PlanningPokerBot.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;

@Slf4j
@Service
public class HelpCommandHandler  implements CommandHandler{
    @Override
    public String getCommandName() {
        return "/help";
    }

    @Override
    public void handle(Update update, PlanningPokerBot bot) {
        long chatId = update.getMessage().getChatId();

        String helpMessage = """
            ⚡ Добро пожаловать в бота покерного планировани!
            
            Доступные команды:
            
            /start - Начать взаимодействие
            /register - Регистрация
            /menu - Вызов меню
            /help - Список доступных команд
            """;
        bot.sendMessage(chatId, helpMessage);

    }
}
