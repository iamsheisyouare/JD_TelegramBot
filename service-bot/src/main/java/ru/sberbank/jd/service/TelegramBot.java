package ru.sberbank.jd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sberbank.jd.config.BotConfig;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.handler.EmployeeApiHandler;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;

    final IntegrationConfig integrationConfig;

    static final String HELP_TEXT = "Этот бот создан для помощи сотрудникам.\n\n" +
            "Вы можете выбрать команды в меню или напечатать:\n\n" +
            "/start - приветственное сообщение\n\n" +
            "/chat - доступ в чат сотрудников\n\n" +
            "/vacations - отображает текущий график отпусков пользователя\n\n" +
            "/new_vacation - заявка на новый отпуск\n\n" +
            "/help - отображает данное сообщение";

    static final String ERROR_TEXT = "Error occurred: ";
    private final RestTemplate restTemplate;

    private String telegramName;
    private String userFirstName;
    private String userLastName;

    public TelegramBot(BotConfig botConfig, IntegrationConfig integrationConfig, RestTemplate restTemplate) {
        this.botConfig = botConfig;
        this.integrationConfig = integrationConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * @return
     */
    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    /**
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            EmployeeApiHandler employeeApiHandler = new EmployeeApiHandler(restTemplate, integrationConfig);
            EmployeeResponse employeeResponse = new EmployeeResponse();

            telegramName = update.getMessage().getChat().getUserName();
            userFirstName = update.getMessage().getChat().getFirstName();
            userLastName = update.getMessage().getChat().getLastName();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, telegramName);
                    break;
                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;
                case "/userByName":
                    employeeResponse = employeeApiHandler.getEmployeeByTelegramName("oduvan");
                    prepareAndSendMessage(chatId, "Find by Name | ID = " + employeeResponse.getId() + " | token = " + employeeResponse.getToken());
                    break;
                case "/userById":
                    employeeResponse = employeeApiHandler.getEmployeeById(1L);
                    prepareAndSendMessage(chatId, "Find by ID | ID = " + employeeResponse.getId() + " | token = " + employeeResponse.getToken());
                    break;
                case "/newUser":
                    String userFIO = userFirstName + " " + userLastName;
                    employeeApiHandler.createEmployee(telegramName, userFIO);
                    prepareAndSendMessage(chatId, "Create new Employee");
                    break;
                default:
                    sendMessage(chatId, "Извините! Пока не поддерживается!");
                    break;
            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + ", рад приветствовать тебя в боте!";
        //log.info("Replied to user " + name );

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        }
        catch (TelegramApiException e){
            //log.error(ERROR_TEXT + e.getMessage());
        }

    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            //log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }


}
