package rest.message.saving.app.demo;

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
    void messageSendingTest() {
        List<Message> sentMessages = sentMessageSaveRequestsRequest();

        int deliveredCount = 0;
        int notDeliveredCount = 0;
        int pagesCount = MESSAGES_COUNT / 10;
        pagesCount = MESSAGES_COUNT % 10 == 0 ? --pagesCount : pagesCount;

        for (Message sentMessage : sentMessages) {
            boolean messageWasFind = false;
            boolean notificationWasFind = false;
            Notification foundNotification = null;

            for (int page = pagesCount; page >= 0; page--) {
                Optional<Message> optionalMessageFromApi = findMessage(sentMessage, page);
                Optional<Notification> optionalNotificationFromApi = findNotification(page, sentMessage);
                if (optionalMessageFromApi.isPresent()) messageWasFind = true;
                if (optionalNotificationFromApi.isPresent()) {
                    notificationWasFind = true;
                    foundNotification = optionalNotificationFromApi.get();
                }
                if (messageWasFind && notificationWasFind) {
                    if (foundNotification.getDelivered())
                        deliveredCount++;
                    else
                        notDeliveredCount++;
                    break;
                }
            }
            if (messageWasFind && !notificationWasFind)
                throw new RuntimeException(String.format("Notification was not created for Message with content:'%s'", sentMessage.getMessageContent()));
            else if (!messageWasFind && notificationWasFind)
                throw new RuntimeException(String.format("Notification was created for unsaved Message with content:'%s'", sentMessage.getMessageContent()));
            else if (!messageWasFind)
                notDeliveredCount++;

        }

        logStatistic(deliveredCount, notDeliveredCount);
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

    private Optional<Notification> findNotification(int page, Message message) {
        return Arrays.stream(getNotifications(page)).filter(n -> n.getMessage().getMessageContent().equals(message.getMessageContent())).findFirst();
    }

    private Optional<Message> findMessage(Message sentMessage, int page) {
        return Arrays.stream(getMessages(page))
                .filter(m -> m.getMessageContent().equals(sentMessage.getMessageContent()))
                .findFirst();
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
