package ru.sicuro.PlanningPokerBot.service;

public enum UserState {
    REGISTRATION_WAITING_FOR_NAME,
    TEAM_WAITING_FOR_NAME,
    TEAM_WAITING_FOR_NEW_NAME,
    ADDING_TASK_NAME,
    ADDING_TASK_DESCRIPTION,
    ADDING_TASK_LINK,
    TASK_WAITING_VOTE,
    TEAM_WAITING_DELETE
}
