package ru.sicuro.PlanningPokerBot.service.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;

public interface CommandHandler {

    /**Возвращает имя команды*/
    String getCommandName();

    void handle(Update update, PlanningPokerBot bot);
}
