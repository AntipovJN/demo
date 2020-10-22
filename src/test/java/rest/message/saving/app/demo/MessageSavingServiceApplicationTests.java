package rest.message.saving.app.demo;

import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import rest.message.saving.app.demo.entity.Message;
import rest.message.saving.app.demo.entity.Notification;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MessageSavingServiceApplicationTests {

    private static final String SAVE_MESSAGE_URI = "http://localhost:8080/message";
    private static final String GET_MESSAGES_URI = "http://localhost:8080/messages?page=%s";
    private static final String GET_NOTIFICATIONS_URI = "http://localhost:8080/notifications?page=%s";
    private static final int MESSAGES_COUNT = 1000;
    private HttpHeaders headers;
    private RestTemplate restTemplate;

    @PostConstruct
    void init() {
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
    }

    @Test
    void messageSendingTest() throws InterruptedException {
        List<Message> sentMessages = sentMessageSaveRequestsRequest();

        int deliveredCount = 0;
        int notDeliveredCount = 0;

        waitUntilMessagesSave();
        Pair<List<Message>, List<Notification>> messagesAndNotifications = extractMessagesAndNotificationsFromApi(MESSAGES_COUNT / 10);
        List<Message> messagesFromApi = messagesAndNotifications.getKey();
        List<Notification> notificationsFromApi = messagesAndNotifications.getValue();

        int notificationLack = messagesFromApi.size() - notificationsFromApi.size();
        if (notificationLack > 0)
            throw new RuntimeException(notificationLack + " notification was not created for Message with content:'%s'");
        if (notificationLack < 0)
            throw new RuntimeException(notificationLack + " notification was created for unsaved Message");

        for (Message sentMessage : sentMessages) {
            if (isMessagePresent(messagesFromApi, sentMessage) && isNotificationPresentAndDelivered(notificationsFromApi, sentMessage))
                deliveredCount++;
            else notDeliveredCount++;
        }

        logStatistic(deliveredCount, notDeliveredCount);
    }

    private boolean isMessagePresent(List<Message> messagesFromApi, Message sentMessage) {
        return messagesFromApi.stream().anyMatch(m -> m.getMessageContent().equals(sentMessage.getMessageContent()));
    }

    private boolean isNotificationPresentAndDelivered(List<Notification> notificationsFromApi, Message sentMessage) {
        return notificationsFromApi.stream().anyMatch(n -> n.getMessage().getMessageContent().equals(sentMessage.getMessageContent()) && n.getDelivered());
    }

    private void waitUntilMessagesSave() throws InterruptedException {
        boolean isMessageSaved = false;
        while (!isMessageSaved) {
            isMessageSaved = Objects.requireNonNull(restTemplate.getForObject("http://localhost:8080/isready", Boolean.class));

        }
    }

    private Pair<List<Message>, List<Notification>> extractMessagesAndNotificationsFromApi(int pagesCount) {
        List<Message> messagesFromApi = new ArrayList<>();
        List<Notification> notificationsFromApi = new ArrayList<>();

        for (int page = 0; page < pagesCount; page++) {
            messagesFromApi.addAll(Arrays.asList(getMessages(page)));
            notificationsFromApi.addAll(Arrays.asList(getNotifications(page)));
        }

        return new Pair<>(messagesFromApi, notificationsFromApi);
    }

    private void logStatistic(int deliveredCount, int notDeliveredCount) {
        double deliveredCoefficient = (double) deliveredCount / MESSAGES_COUNT * 100;
        double notDeliveredCoefficient = (double) notDeliveredCount / MESSAGES_COUNT * 100;

        Assertions.assertEquals(MESSAGES_COUNT, deliveredCount + notDeliveredCount);

        log.info("Delivered:" + deliveredCoefficient + "% ;");
        log.info("Not delivered:" + notDeliveredCoefficient + "% ;");
    }

    private List<Message> sentMessageSaveRequestsRequest() {
        List<Message> sentMessages = new ArrayList<>();
        for (int iterationNumber = 0; iterationNumber < MESSAGES_COUNT; iterationNumber++) {
            Message message = new Message();
            message.setMessageContent("iteration_" + iterationNumber);
            try {
                saveMessage(message);
            } catch (HttpServerErrorException.InternalServerError e) {
                log.warn("Message was not delivered");
            }
            sentMessages.add(message);
        }
        return sentMessages;
    }

    private void saveMessage(Message message) {
        HttpEntity<String> request = new HttpEntity<>(message.toString(), headers);
        restTemplate.postForObject(URI.create(SAVE_MESSAGE_URI), request, Exception.class);
    }

    private Message[] getMessages(int page) {
        return Objects.requireNonNull(restTemplate.getForObject(String.format(GET_MESSAGES_URI, page), Message[].class));
    }

    private Notification[] getNotifications(int page) {
        return Objects.requireNonNull(restTemplate.getForObject(String.format(GET_NOTIFICATIONS_URI, page), Notification[].class));
    }
}
