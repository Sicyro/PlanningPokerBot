package ru.sicuro.PlanningPokerBot.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Handler {

    void handle(Update update, PlanningPokerBot bot);

}
