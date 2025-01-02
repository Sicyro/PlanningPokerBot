package ru.sicuro.PlanningPokerBot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PlanningPokerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanningPokerBotApplication.class, args);
		log.info("Bot is being started!");
	}


}
