package ru.sicuro.PlanningPokerBot.service.command;

import ru.sicuro.PlanningPokerBot.service.Handler;

public interface CommandHandler extends Handler {

    /**Возвращает имя команды*/
    String getCommandName();

}
