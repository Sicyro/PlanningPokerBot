package ru.sicuro.PlanningPokerBot.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface StepHandler {

    void handleStep(Update update, PlanningPokerBot bot);

}
