package ru.sicuro.PlanningPokerBot.service.button;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.sicuro.PlanningPokerBot.service.PlanningPokerBot;

public interface ButtonHandler {

    String getCallbackData();

    void handle(Update update, PlanningPokerBot bot);
}
