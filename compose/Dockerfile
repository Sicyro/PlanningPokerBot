# Используем базовый образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY imports/PlanningPokerBot*.jar /app/PlanningPokerBot.jar

# Указываем команду для выполнения JAR-файла
CMD ["java", "-jar", "/app/PlanningPokerBot.jar", "--spring.profiles.active=prod"]