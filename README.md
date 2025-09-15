# job4j_url_shortcut
Spring Boot приложение, которое все ссылки на сайте заменяет ссылками на наш сервис.
Сервис работает через REST API.

# Стек технологий:
Java 19  
PostgreSQL 16.2  
Spring Boot 2.7.6  
Maven 3.9.6  
Liquibase Maven Plugin 4.15.0  

# Требования к окружению
Microsoft Windows 11  
Java 19  
PostgreSQL 16

# Запуск проекта:
Создать локальную копию проекта клонированием из репозитория https://github.com/GitHubfilipich/job4j_url_shortcut  
В PostgreSQL создать базу данных и в папке проекта в файле 
"...\src\main\resources\application.properties" указать её адрес (url), имя пользователя (username) и
пароль (password).  
В терминале в папке проекта выполнить скрипты создания БД командой
"mvn liquibase:update -Pproduction".  
Создать исполняемый файл проекта "job4j_url_shortcut-1.0-SNAPSHOT.jar" в папке "target" проекта командой
"mvn clean package -Pproduction -DskipTests".  
Запустить исполняемый файл командой "java -jar target/job4j_url_shortcut-1.0-SNAPSHOT.jar".  

# Взаимодействие с приложением:
Взаимодействие с приложением происходит через REST API.

# Контакты
https://github.com/GitHubfilipich